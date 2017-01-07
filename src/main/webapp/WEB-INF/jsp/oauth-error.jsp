<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <div class="text-content">
        <h2 class="form-heading">Authorization Error</h2>
        <p>An unexpected error occurred during the authorization process.</p>

        <div id="chips">
            <span class="chip">${errorSummary}</span>
        </div>
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