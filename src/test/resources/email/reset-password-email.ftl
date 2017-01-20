<html>
<body>
<p>Dear ${user.lastName}, ${user.firstName}</p>

<p> Somebody recently asked to reset your Facebook password.</p>
<p>Please enter the following password reset code:</p>

<p style="border:1px solid red">
    ${user.verifyEmailCode}
</p>

<p>
    Thanks,<br/>
    Launchpad Team
</p>

</body>
</html>