require([
      "dojo/_base/lang",
      "ecm/model/Desktop",
      "dojo/aspect",
      "ecm/model/Request",
      "ecm/widget/dialog/MessageDialog",
      "ecm/widget/dialog/SessionExpireWarningDialog",
      "dHSSessionManagerPluginDojo/widget/dialog/PluginSessionDialog"
   ],
   function (lang, Desktop, aspect, Request, MessageDialog, SessionExpireWarningDialog, PluginSessionDialog) {
      // check the login session when user click on login ..
      var userId = null;
      aspect.after(Desktop, "onLogin", lang.hitch(this, function () {
            // check if a user is already logged in
            console.log('on desktop login with aspect ');
            userId = ecm.model.desktop.userId; // maintain an instance of it
            var params = {
               "userId": ecm.model.desktop.userId,
               "type": "GetSession"
            };
            Request.invokePluginService("DHSSessionManagerPlugin", "ManagerService", {
                  requestParams: params,
                  requestCompleteCallback: lang.hitch(this, function (d) {
                        console.dir(d);
                        if (d && (d.status === "Success" && d.isLogged == true)) {
                           // already loggedIn so needs to show message box
                           var dialog = new PluginSessionDialog();
                           dialog.show();
                        } else if (d && (d.status === "Success" && d.isLogged == false)) {
                           // add new session with current user id
                           addNewSession();
                        }
                  }),
               requestFailedCallback: lang.hitch(this, function (d) {
                  if (d && d.status === "Failed") {
                     // show error message & do immediate logout
                     var message = new MessageDialog({
                        text: 'An error occurred while creating a session object. However you can continue to work (but session is not managed by system)',
                        buttonLabel: 'Ok'
                     });
                     message.show();
                  }
               })
            });
      }));


   aspect.before(SessionExpireWarningDialog,"_onLogoff",lang.hitch(this,function() {
     console.log('on session expiry logoff with aspect ');
          var params = {
             userId: userId != "" ? userId : ecm.model.desktop.userId,
             type: "DisableSession"
          };
          Request.invokePluginService("DHSSessionManagerPlugin", "ManagerService", {
             requestParams: params,
             requestCompleteCallback: lang.hitch(this, function (d) {
                console.log('session is removed onLogout');
                console.dir(d);
             }),
             requestFailedCallback: lang.hitch(this, function (d) {
                console.log('An error occurred while removing session onLogout');
                console.dir(d);
             })
          });
   }));
   aspect.after(Desktop, "onLogout", lang.hitch(this, function () {
      console.log('on desktop logout with aspect ');
      var params = {
         userId: userId != "" ? userId : ecm.model.desktop.userId,
         type: "DisableSession"
      };
      Request.invokePluginService("DHSSessionManagerPlugin", "ManagerService", {
         requestParams: params,
         requestCompleteCallback: lang.hitch(this, function (d) {
            console.log('session is removed onLogout');
            console.dir(d);
         }),
         requestFailedCallback: lang.hitch(this, function (d) {
            console.log('An error occurred while removing session onLogout');
            console.dir(d);
         })
      });
   }));

   function addNewSession() {
      var params = {
         "userId": ecm.model.desktop.userId,
         "type": "AddSession"
      };
      Request.invokePluginService("DHSSessionManagerPlugin", "ManagerService", {
         requestParams: params,
         requestCompleteCallback: lang.hitch(this, function (d) {
            console.log('session is added for user ' + params.userId);
            console.dir(d);
         }),
         requestFailedCallback: lang.hitch(this, function (d) {
            console.log('An error occurred while adding session onLogin');
            console.dir(d);
         })
      });
   }

 } // main plugin function
);