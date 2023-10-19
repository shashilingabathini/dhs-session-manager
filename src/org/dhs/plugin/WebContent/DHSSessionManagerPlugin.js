require([
        "dojo/_base/lang",
        "ecm/model/Desktop",
        "dojo/aspect",
        "ecm/model/Request"
    ],
    function(lang, Desktop , aspect , Request) {
        // check the login session when user click on login ..
        aspect.after(Desktop,"onLogin", lang.hitch(this,function() {
            // check if a user is already logged in
            Request.invokePluginService("DHSSessionManagerPlugin","ManagerService",{
                requestParams: params,
                requestCompleteCallback : lang.hitch(this,function(d) {
                    if(d && (d.status === "Success"  && d.isLogged === "T")) {
                        // already loggedIn so needs to show message box
                    }
                }),
                requestFailedCallback : lang.hitch(this,function(d) {
                    if(d && d.status === "Failed") {
                        // show error message & do immediate logout
                    }
                })
            });
        }));
    }
);