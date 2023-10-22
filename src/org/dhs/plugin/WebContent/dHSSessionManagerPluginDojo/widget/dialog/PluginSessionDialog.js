define([
    "dojo/_base/declare",
    "ecm/widget/dialog/BaseDialog",
    "ecm/model/Request",
    "dojo/_base/lang",
    "dojo/text!./templates/PluginSessionDialog.html"
], function(declare, BaseDialog, Request, lang, template) {

    return declare("dHSSessionManagerPluginDojo.widget.dialog.PluginSessionDialog", [BaseDialog] , {
        contentString: template,
        widgetsInTemplate: true,
        postCreate: function() {
            this.inherited(arguments);
            this.setTitle('<div> <b>Session Error</b> </div>');
            this.setSize(500,400);
            this.setMaximized(false);
            if (this.cancelButton)
            	this.cancelButton.domNode.style.display = "None";

            if(this.description)
               this.description.innerHTML =  "We observed an active session already available with your id or you are not allowed to use this repository <br/> <b>Note: </b> Only one session allowed per user and allowed users only can use repository.";

            this.addButton("Ok", this._onOkClick, false);
        },
        _onOkClick : function() {
            var params = {
                     userId:  ecm.model.desktop.userId,
                     type: "DisableSession"
                  };
             Request.invokePluginService("DHSSessionManagerPlugin", "ManagerService", {
                 requestParams: params,
                 requestCompleteCallback: lang.hitch(this, function (d) {
                    console.log('session is removed onLogout');
                    console.dir(d);
                    ecm.model.desktop.logoff(true);
                    this.hide();
                    this.destroy();
                 }),
                 requestFailedCallback: lang.hitch(this, function (d) {
                    console.log('An error occurred while removing session onLogout');
                    console.dir(d);
                 })
            });
      }
    });

});