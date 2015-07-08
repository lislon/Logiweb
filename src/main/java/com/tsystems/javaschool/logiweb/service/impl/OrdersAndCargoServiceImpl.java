package com.tsystems.javaschool.logiweb.service.impl;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.tsystems.javaschool.logiweb.dao.CargoDao;
import com.tsystems.javaschool.logiweb.dao.CityDao;
import com.tsystems.javaschool.logiweb.dao.DeliveryOrderDao;
import com.tsystems.javaschool.logiweb.dao.exceptions.DaoException;
import com.tsystems.javaschool.logiweb.model.Cargo;
import com.tsystems.javaschool.logiweb.model.City;
import com.tsystems.javaschool.logiweb.model.DeliveryOrder;
import com.tsystems.javaschool.logiweb.model.Driver;
import com.tsystems.javaschool.logiweb.model.Truck;
import com.tsystems.javaschool.logiweb.model.status.CargoStatus;
import com.tsystems.javaschool.logiweb.model.status.OrderStatus;
import com.tsystems.javaschool.logiweb.service.OrdersAndCargoService;
import com.tsystems.javaschool.logiweb.service.exceptions.LogiwebServiceException;
import com.tsystems.javaschool.logiweb.service.exceptions.ServiceValidationException;

/**
 * Data manipulation and business logic related to 
 * orders and cargoes.
 * 
 * @author Andrey Baliushin
 */
public class OrdersAndCargoServiceImpl implements OrdersAndCargoService {

    private static final Logger LOG = Logger.getLogger(OrdersAndCargoServiceImpl.class);
        
    private DeliveryOrderDao deliveryOrderDao;
    private CargoDao cargoDao;
    private CityDao cityDao;
    private EntityManager em;

    public OrdersAndCargoServiceImpl(DeliveryOrderDao deliveryOrderDao,
            CargoDao cargoDao, CityDao cityDao, EntityManager em) {
        this.deliveryOrderDao = deliveryOrderDao;
        this.cargoDao = cargoDao;
        this.cityDao = cityDao;
        this.em = em;
    }

    
    private EntityManager getEntityManager() {
        return em;
    }


    @Override
    public Set<DeliveryOrder> findAllOrders() throws LogiwebServiceException {
        try {
            getEntityManager().getTransaction().begin();
            Set<DeliveryOrder> orders = deliveryOrderDao.findAll();
            getEntityManager().getTransaction().commit();
            return orders;
        } catch (DaoException e) {
            LOG.warn("Something unexcpected happend.");
            throw new LogiwebServiceException(e);
        } finally {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
        }
    }

    @Override
    public DeliveryOrder addNewOrder(DeliveryOrder newOrder)
            throws LogiwebServiceException {
        try {
            getEntityManager().getTransaction().begin();
            deliveryOrderDao.create(newOrder);
            getEntityManager().getTransaction().commit();

            LOG.info("Order created. ID#" + newOrder.getId());

            return newOrder;
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
        }
    }


    @Override
    public DeliveryOrder findOrderById(int id) throws LogiwebServiceException {
        try {
            getEntityManager().getTransaction().begin();
            DeliveryOrder order = deliveryOrderDao.find(id);
            getEntityManager().getTransaction().commit();

            return order;
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addCargo(Cargo newCargo) throws ServiceValidationException, LogiwebServiceException {
        try {
            validateNewCargoForEmptyFields(newCargo);
        } catch (ServiceValidationException e) {
            throw e;
        }
        
        try {
            getEntityManager().getTransaction().begin();
            
            //get managed entities
            City originCity = cityDao.find(newCargo.getOriginCity().getId());
            City destinationCity = cityDao.find(newCargo.getDestinationCity()
                    .getId());
            DeliveryOrder orderForCargo = deliveryOrderDao.find(newCargo
                    .getOrderForThisCargo().getId());

            //switch detached entities in cargo to managed ones
            newCargo.setOriginCity(originCity);
            newCargo.setDestinationCity(destinationCity);
            newCargo.setOrderForThisCargo(orderForCargo);
            
            validateCargoManagedFieldsByBusinessRequirements(newCargo);            
            
            newCargo.setStatus(CargoStatus.WAITING_FOR_PICKUP);
            
            cargoDao.create(newCargo);
            LOG.info("New cargo with id #" + newCargo.getId() + "created for irder id #" + orderForCargo.getId());
            getEntityManager().refresh(newCargo.getOrderForThisCargo());
            getEntityManager().getTransaction().commit();
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
        }
    }
    
   /**
    * Validate cargo (fields must be managed)
    * 
    * Req.:
    * Both destination an origin cities must exist, and must not be the same city.
    * Order for this cargo must exist.
    * Order must be with status NOT READY
    * @param c cargo where all fields are managed entities.
    * @throws ServiceValidationException if requirements are not met. 
    */
    private void validateCargoManagedFieldsByBusinessRequirements(Cargo c) throws ServiceValidationException {
        if (c.getOriginCity() == null) {
            throw new ServiceValidationException("Origin city does not exist.");
        } else if (c.getDestinationCity() == null) {
            throw new ServiceValidationException("Destination city does not exist.");
        } else if (c.getOriginCity().equals(c.getDestinationCity())) {
            throw new ServiceValidationException("Cities must be different.");
        } else if (c.getOrderForThisCargo() == null) {
            throw new ServiceValidationException("Order for this cargo does not exist.");
        } else if (c.getOrderForThisCargo().getStatus() != OrderStatus.NOT_READY) {
            throw new ServiceValidationException("Order must be in NOT READY status to add new cargo.");
        }
    }
    
    /**
     * Validate that cities, title, weight, and order fields are not empty or null.
     * @param c
     * @throws ServiceValidationException if validation failed.
     */
    private void validateNewCargoForEmptyFields(Cargo c) throws ServiceValidationException {
        if (StringUtils.isBlank(c.getTitle())) {
            throw new ServiceValidationException("Cargo title can't be blank.");
        } else if (c.getWeight() <= 0f) {
            throw new ServiceValidationException("Cargo weight must be greater than zero.");
        } else if (c.getOriginCity() == null) {
            throw new ServiceValidationException("Origin city must be specified.");
        } else if (c.getOriginCity() == null) {
            throw new ServiceValidationException("Desitnation city must be specified.");
        } else if (c.getOrderForThisCargo() == null) {
            throw new ServiceValidationException("Delivery Order must be specified.");
        }
    }

    @Override
    public void setStatusOrderReadyToGo(DeliveryOrder order)
            throws LogiwebServiceException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void findAvailiableDriversForOrder(DeliveryOrder order)
            throws LogiwebServiceException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public Set<Truck> findAvailiableTrucksForOrder(DeliveryOrder order)
            throws LogiwebServiceException {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
