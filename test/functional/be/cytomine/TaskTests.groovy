package be.cytomine

import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.TaskAPI
import be.cytomine.utils.Task
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class TaskTests  {

    void testShowTask() {
        println "test task"
        Task task = new Task(projectIdent: BasicInstanceBuilder.getProject().id,userIdent:  BasicInstanceBuilder.getUser().id)
        task.progress = 50
        task = task.saveOnDatabase()
        task.addComment("First step...")
        task.addComment("Second step...")

        println "task.progress="+task.progress
        println "task.comments="+task.getMap()


        def result = TaskAPI.show(task.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println "task.json="+json
        assert json.progress == 50
        assert json.comments.size()==2
        assert json.comments[1].equals("First step...")
        assert json.comments[0].equals("Second step...")

        assert json.project == task.projectIdent
        assert json.user == task.userIdent
    }

    void testShowTaskNotExist() {
        def result = TaskAPI.show(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testTask() {
        Task task = new Task(projectIdent: BasicInstanceBuilder.getProject().id,userIdent:  BasicInstanceBuilder.getUser().id)
        assert task.progress==0
        task = task.saveOnDatabase()
        task.addComment("First step...")
        task.addComment("Second step...")
        assert task.getLastComments(5).size()==2
    }

    void testAddTask() {
        Project project = BasicInstanceBuilder.getProject()
        def result = TaskAPI.create(project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        assert json instanceof JSONObject
        println "task.json="+json
        assert json.task.progress == 0
        //assert json.comments.size()==1
        assert json.task.project == project.id
    }

    void testConcreteTask() {
        //create a job
        Job job = BasicInstanceBuilder.getJobNotExist(true)
        BasicInstanceBuilder.getSoftwareProjectNotExist(job.software,job.project,true)

        UserJob userJob = BasicInstanceBuilder.getUserJobNotExist(true)
        userJob.job = job
        userJob.user = BasicInstanceBuilder.getUser()
        BasicInstanceBuilder.saveDomain(userJob)

        //add algo-annotation for this job
        AlgoAnnotation a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist(job,userJob,true)

        //add algo-annotation-term for this job
        AlgoAnnotationTerm at1 = BasicInstanceBuilder.getAlgoAnnotationTerm(job,a1,userJob)

        def result = TaskAPI.create(job.project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def jsonTask = JSON.parse(result.data)


        //delete all job data
        result = JobAPI.deleteAllJobData(job.id, jsonTask.task.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def task = new Task().getFromDatabase(jsonTask.task.id)
        assert task.progress==100
    }

    void testListTaskCommentForProject() {
        Project project = BasicInstanceBuilder.getProject()
        def result = TaskAPI.create(project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def idtask = JSON.parse(result.data).task.id
        Task task = new Task()
        task = task.getFromDatabase(idtask)
        task.addComment("test")

       result = TaskAPI.listByProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
   }

}
