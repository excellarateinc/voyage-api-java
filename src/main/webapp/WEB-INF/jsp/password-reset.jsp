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

    <title>Password Reset</title>

    <link href="${contextPath}/webjars/bootstrap/css/bootstrap.min.css" rel='stylesheet'>
    <link href="${contextPath}/resources/css/common.css" rel="stylesheet">

    <script type="application/javascript">
        function validatePassword() {
            var password = document.passwordForm.password.value;
            var confirmPassword = document.passwordForm.confirmPassword.value;
            var uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            var lowercase = "abcdefghijklmnopqrstuvwxyz";
            var digits = "0123456789";
            var splChars ="!\"#$%&'()*+,-./:;<=>?@[]^_`{|}~";
            var uppercaseFlag = contains(password, uppercase);
            var lowercaseFlag = contains(password, lowercase);
            var digitsFlag = contains(password, digits);
            var charsFlag = contains(password, splChars);
            var lengthFlag = password.length >= 8 && password.length <= 100;
            var confirmFlag = password === confirmPassword;

            togglePasswordValidationImages('uppercase', uppercaseFlag);
            togglePasswordValidationImages('lowercase', lowercaseFlag);
            togglePasswordValidationImages('digit', digitsFlag);
            togglePasswordValidationImages('symbol', charsFlag);
            togglePasswordValidationImages('length', lengthFlag);
            togglePasswordValidationImages('confirm', confirmFlag);

            return uppercaseFlag && lowercaseFlag && digitsFlag && charsFlag && lengthFlag && confirmFlag;
        }

        function contains(password, allowedChars) {
            for (i = 0; i < password.length; i++) {
                var char = password.charAt(i);
                if (allowedChars.indexOf(char) >= 0) { return true; }
            }
            return false;
        }

        function togglePasswordValidationImages(prefix, isValid) {
            var validImg = document.getElementById(prefix + "-valid");
            var invalidImg = document.getElementById(prefix + "-invalid");
            if (isValid) {
                invalidImg.className = "invisible";
                validImg.className = "";
            } else {
                invalidImg.className = "";
                validImg.className = "invisible";
            }
        }
    </script>
</head>

<body>

<div class="container">

    <form name="passwordForm" method="POST" action="${contextPath}/oauth/password-reset" class="form-signin form-password" onsubmit="return validatePassword()">

        <img id="logo" src="/resources/images/voyage-logo-vert-fc.png" />

        <h2 class="form-heading">Reset Your Password</h2>

        <c:if test="${resetSuccess}">
            <h4>Your password has been reset! <br /> <br /> You are ready to login with your new password.</h4>

            <c:if test="${loginRedirectUri}">
                <button class="btn btn-lg btn-primary btn-block" type="submit">Login</button>
            </c:if>

        </c:if>

        <c:if test="${!resetSuccess}">

            <c:if test="${weakPassword}">
                <h4>The password you entered is not valid</h4>
            </c:if>

            <div id="password-requirements">
                Requirements must have at least:
                
                <ul>
                    <li>
                        <img id="uppercase-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="uppercase-invalid" src="/resources/images/password-invalid.png" />
                        1 Upper Case letter (example: A B C D)
                    </li>
                    <li>
                        <img id="lowercase-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="lowercase-invalid" src="/resources/images/password-invalid.png" />
                        1 lower case letter (example: a s d f)
                    </li>
                    <li>
                        <img id="digit-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="digit-invalid" src="/resources/images/password-invalid.png" />
                        1 digit (example: 1 2 3 4)
                    </li>
                    <li>
                        <img id="symbol-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="symbol-invalid" src="/resources/images/password-invalid.png" />
                        1 symbol (example: ! " # $ % &)
                    </li>
                    <li>
                        <img id="length-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="length-invalid" src="/resources/images/password-invalid.png" />
                        Between 8 - 100 characters
                    </li>
                    <li>
                        <img id="confirm-valid" src="/resources/images/password-valid.png" class="invisible" />
                        <img id="confirm-invalid" src="/resources/images/password-invalid.png" />
                        Password matches Confirm Password
                    </li>
                </ul>
            </div>

            <div class="form-group">
                <input name="password" type="password" class="form-control" placeholder="New Password" autofocus="autofocus" onkeyup="validatePassword()" />
                <input name="confirmPassword" type="password" class="form-control" placeholder="Confirm Password" onkeyup="validatePassword()" />
                <button class="btn btn-lg btn-primary btn-block" type="submit">Reset Password</button>
            </div>
        </c:if>

        <div id="support-footer">
            For assistance, contact the ${appName} support team via
            <a href="tel:${supportPhone}">phone</a>,
            <a href="mailto:${supportEmail}">email</a>, or
            <a href="${supportWebsite}">website</a>.
        </div>
    </form>

</div>
<!-- /container -->
</body>
</html>