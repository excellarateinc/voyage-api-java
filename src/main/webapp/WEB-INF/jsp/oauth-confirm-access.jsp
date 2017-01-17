<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<spring:eval expression="@environment.getProperty('app.name')" var="appName" />
<spring:eval expression="@environment.getProperty('app.contact-support.email')" var="supportEmail" />
<spring:eval expression="@environment.getProperty('app.contact-support.phone')" var="supportPhone" />
<spring:eval expression="@environment.getProperty('app.contact-support.website')" var="supportWebsite" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

    <title>Grant Approval</title>

    <link href="${contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel='stylesheet'>
    <link href="${contextPath}/resources/css/common.css" rel="stylesheet">
</head>

<body>

<div class="container">
    <div class="form-signin">
        <h2 class="form-heading">Grant Approval</h2>
        <p>Do you authorize '${authorizationRequest.clientId}' to perform the following tasks?</p>

        <div class="chips">
            <c:forEach items="${authorizationRequest.scope}" var="scope">
                <span class="chip">${scope}</span>
            </c:forEach>
        </div>

        <div class="form-group">
            <form id='confirmationForm' name='confirmationForm' action='${contextPath}/oauth/authorize' method='post'>
                <input name='user_oauth_approval' value='true' type='hidden'/>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Approve</button>
            </form>
        </div>

        <div class="form-group">
            <form id='denialForm' name='denialForm' action='${contextPath}/oauth/authorize' method='post'>
                <input name='user_oauth_approval' value='false' type='hidden'/>
                <button class="btn btn-lg btn-danger btn-block" type="submit">Deny</button>
            </form>
        </div>

        <div id="support-footer">
            For assistance, contact the ${appName} support team via
            <a href="tel:${supportPhone}">phone</a>,
            <a href="mailto:${supportEmail}">email</a>, or
            <a href="${supportWebsite}">website</a>.
        </div>
    </div>
</div>
<!-- /container -->

</body>
</html>