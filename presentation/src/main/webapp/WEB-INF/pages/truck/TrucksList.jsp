<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:include page="../GlobalHeader.jsp">
    <jsp:param name="title" value="Trucks List" />
    <jsp:param value="common/common.css" name="css"/>
    <jsp:param value="common/RemoveRecord.js" name="js"/>
    
</jsp:include>

<jsp:include page="../GlobalHeaderMenu.jsp">
    <jsp:param name="homeLink" value="/" />
    <jsp:param name="userRoleForTitle" value="Manager" />
</jsp:include>

<div class="panel panel-default">
    <div class="panel-heading"><h1>List of trucks</h1></div>
    <div class="panel-body">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>License plate</th>
					<th>Crew size</th>
					<th>Cargo capacity <small>x1000kg<small></th>
					<th>Status</th>
					<th>Current City</th>
					<th>Delivery order</th>
					<th>Drivers</th>
					<th class="text-center">Edit</th>
                    <th class="text-center">Delete</th>
				</tr>
			</thead>
			<tbody>
		
				<c:forEach items="${trucks}" var="truck">
					<tr id="truck-id-${truck.id}-row">
					
						<td>${truck.licencePlate}</td>
						<td>${truck.crewSize}</td>
						<td>${truck.cargoCapacity}</td>
						<td>${truck.status}</td>
						<td>${cities[truck.currentCityId].name}</td>
						
						<td><c:if test="${empty truck.assignedDeliveryOrderId}">Not assigned</c:if>
	                            <a href="${pageContext.request.contextPath}/order/${truck.assignedDeliveryOrderId}">
	                                ${truck.assignedDeliveryOrderId}
	                            </a>
                        </td>
							
						<td><c:if test="${empty truck.driversIdsAndSurnames}">Not assigned</c:if>
                            <c:forEach items="${truck.driversIdsAndSurnames}" var="entry">
							    <a href="
		                            <c:url value="/driver/${entry.key}">
		                            </c:url>">${entry.value}</a><span class="comma-separator">,</span>
							</c:forEach>
                        </td>	
                            
						<td class="text-center">
						  <c:choose>
						      <c:when test="${empty truck.assignedDeliveryOrderId}">
						          <a
                                href="${pageContext.request.contextPath}/truck/${truck.id}/edit"><span
                                    class="glyphicon glyphicon-pencil" aria-hidden="true"></span></a>
						      </c:when>
						      <c:otherwise>
						          <span
                                    class="glyphicon glyphicon-pencil disabled-color" aria-hidden="true"></span>
						      </c:otherwise>
						  </c:choose>
							 
						</td>
		                
		                <td class="text-center">
		                    <span onclick="removeRecord(this, ${truck.id})" class="glyphicon glyphicon-remove red-on-hover" aria-hidden="true"></span>
		                </td>
		
					</tr>
				</c:forEach>
		
			</tbody>
		</table>
	</div>
	
	<%-- Edit priveleges --%>
    <sec:authorize access="hasRole('ROLE_MANAGER')">
        <div class="panel-footer">
            <a href="${pageContext.request.contextPath}/truck/new" role="button"
                class="btn btn-default btn-large btn-block"><span
                class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add</a>
        </div>
    </sec:authorize>
    
</div>

<jsp:include page="../GlobalFooter.jsp"/>