package be.cytomine.integration

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class NotifyAuroraUploadJob {


    def auroraService

    static triggers = {
        //simple name: 'notifyAuroraUpload', startDelay: 1000, repeatInterval: 1000
    }

    def group = "MyGroup"

    def execute() {
        SpringSecurityUtils.reauthenticate "superadmin", null
        auroraService.notifyImage()
    }
}
