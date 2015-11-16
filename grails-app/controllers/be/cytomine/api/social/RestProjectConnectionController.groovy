package be.cytomine.api.social


/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.project.Project
import be.cytomine.security.SecUser

/**
 * Controller for an user connection to a project
 */
class RestProjectConnectionController extends RestController {

    def cytomineService
    def secUserService
    def projectService
    def projectConnectionService

    def add = {
        try {
            responseSuccess(projectConnectionService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def lastConnectionInProject = {
        Project project = projectService.read(params.project)
        responseSuccess(projectConnectionService.lastConnectionInProject(project));
    }

    def getConnectionByUserAndProject = {
        SecUser user = secUserService.read(params.user)
        Project project = projectService.read(params.project)
        boolean all = params.all
        responseSuccess(projectConnectionService.getConnectionByUserAndProject(user, project, all))
    }

    def numberOfConnectionsByProjectAndUser = {
        SecUser user = secUserService.read(params.user)
        Project project = projectService.read(params.project)
        Long afterThan = params.long("afterThan");

        if(params.boolean('heatmap')) {
            responseSuccess(projectConnectionService.numberOfConnectionsByProjectOrderedByHourAndDays(project, afterThan, user))
        } else {
            responseSuccess(projectConnectionService.numberOfConnectionsByProjectAndUser(project, user))
        }
    }
}
