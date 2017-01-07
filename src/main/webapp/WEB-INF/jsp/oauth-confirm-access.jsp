<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

    <title>Log in with your account</title>

    <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="${contextPath}/resources/css/common.css" rel="stylesheet">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->

    <style type="text/css">
        #chips {
            margin-bottom: 20px;
        }
        .chip {
            display: inline-block;
            padding: 10px 25px 10px 0;
            font-size: 16px;
            font-weight: bold;
        }
    </style>
</head>

<body>

<div class="container">
    <div class="form-signin">
        <h2 class="form-heading">Grant Approval</h2>
        <p>Do you authorize '${authorizationRequest.clientId}' to perform the following tasks?</p>

        <div id="chips">
            <c:forEach items="${authorizationRequest.scope}" var="scope">
                <span class="chip">${scope}</span>
            </c:forEach>
        </div>

        <div class="form-group">
            <form id='confirmationForm' name='confirmationForm' action='${contextPath}/oauth/authorize' method='post'>
                <input name='user_oauth_approval' value='true' type='hidden'/>
                <input name="authorize" value="Authorize" class="btn btn-lg btn-primary btn-block" type="submit" />
            </form>
        </div>

        <div class="form-group">
            <form id='denialForm' name='denialForm' action='${contextPath}/oauth/authorize' method='post'>
                <input name='user_oauth_approval' value='false' type='hidden'/>
                <button class="btn btn-lg btn-danger btn-block" type="submit">Deny</button>
            </form>
        </div>

    </div>
</div>
<!-- /container -->

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="${contextPath}/resources/js/bootstrap.min.js"></script>
</body>
</html>