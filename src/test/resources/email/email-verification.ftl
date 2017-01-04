<html>
<body>
<p>Dear ${user.lastName}, ${user.firstName}</p>

<p> Thank you for signing up for Launchpad. To complete the registration process
    , please click on the link below to confirm your e-mail address and activate your new launchpad.security.account.</p>

<p>
    <a style="background:#62cb31;min-height:36px;margin:10px 0;padding:0 30px;line-height:36px;border-radius:4px;font-size:18px;color:#fff;display:inline-block;text-decoration:none"
       href="http://localhost:9000/#/activate/${token.value}" title="Activate account" target="_blank">Activate Account</a>
</p>

<p>
    Once you see the activation confirmation screen you will be able to login and begin using
    your Launchpad launchpad.security.account.
</p>

<p>
    Thanks,<br/>
    Launchpad Team
</p>

</body>
</html>