<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
  ~ Copyright 2017 Lighthouse Software, Inc.   http://www.LighthouseSoftware.com
  ~
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
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

    <title>Log in with your account</title>

    <link href="${contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel='stylesheet'>
    <link href="${contextPath}/resources/css/common.css" rel="stylesheet">
</head>

<body>

<div class="container">
    <div class="text-content">
        <h2 class="form-heading">Authorization Error</h2>
        <p>An error occurred during the authorization process.</p>

        <div class="chips">
            <span class="chip">${errorSummary}</span>
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