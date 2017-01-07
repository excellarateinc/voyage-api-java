<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

    <title>I think you're lost!</title>

    <link href="${contextPath}/resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="${contextPath}/resources/css/common.css" rel="stylesheet">
</head>

<body>

<div class="container">

    <div class="text-content">
        <h2 class="form-heading">I think you're lost!</h2>

        <p>
            You successfully logged in, but this probably isn't the page you are looking for.
        </p>
        <p>
            Try clicking 'Back' in your app or web browser to see if you can try the
            action again.
        </p>
        <br />
        <p>
            Contact <a href="mailto:support@LighthouseSoftware.com">support@LighthouseSoftware.com</a>
            if you need further assistance.
        </p>

    </div>

</div>
<!-- /container -->
</body>
</html>