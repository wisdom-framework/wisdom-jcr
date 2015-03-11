/* global $, Exception, console*/

/**
 *
 * @class TodoListController
 * @extends HUBU.AbstractComponent
 */

function TodoListController() {
    "use strict";

    var self = this;
    var _hub;
    var _model;
    var _url = "/list/";

    self.name = "TodoListControllerDefault";

    self.getComponentName = function() {
        return self.name;
    };

    self.listRender = null; //RactiveRenderService - TodoListRender

    /**
     * Configure the instance of the TodoListController.
     *
     * @method configure
     * @param {HUBU.hub} theHub
     * @param conf - The TodoListController configuration.
     * @param {map} conf.model - The model link to this TodoListController
     * @param {string} [conf.url='/list'] - The root URL of the todo lists
     */
    self.configure = function(theHub, conf) {
        _hub = theHub;

        if (typeof conf == "undefined") {
            throw new Exception("The TodoListController configuration is mandatory.");
        }

        if (typeof conf.model !== "object") {
            throw new Exception("The model entry is mandatory.");
        }

        //Check with a regexp
        if (typeof conf.url === "string") {
            _url = conf.url;
        }

        _model = conf.model;

        _hub.requireService({
            component: this,
            contract: window.RactiveRenderService,
            field: "listRender"
        });
    };

    function encodeIdURL(root,id) {
      return (root+id).replace("#","%23").replace(":","%3A");
    }

    function newTodo(event) {

        var newtodo = {
            content: event.node.value,
            done: "false"
        };

        $.ajax({
            type: "PUT",
            contentType: "application/json; charset=UTF-8",
            url: _model.url,
            data: JSON.stringify(newtodo)
        }).done(function(data) {
            _model.todos.push(data);
        });
    }

    function updateTodo(event) {
        var todo = event.context;

        $.ajax({
            type: "POST",
            contentType: "application/json; charset=UTF-8",
            url: encodeIdURL(_model.url+"/",todo.id),
            data: JSON.stringify(todo)
        }).done(function() {
            //TODO notification
        });
    }

    function delTodo(event, index) {
        var todo = event.context;

        $.ajax({
            type: "DELETE",
            url: encodeIdURL(_model.url+"/",todo.id)
        }).done(function() {
            _model.todos.splice(index, 1);
        });
    }

    /**
     * @return the number of todos not yet done present in the list model.
     */
    function notDone(todos){
      return todos.reduce(function(prev,curr){return prev + !curr.done;},0);
    }

    self.start = function() {
      _model.notDone = notDone;

        $.ajax(_url).then(function(todolist) {
            if (todolist.length > 0) {
                _model.name = todolist[0].name;
                _model.id = todolist[0].id;
                _model.todos = todolist[0].todos;
                _model.url = encodeIdURL(_url,_model.id);
            }

            //render the todolist
            self.listRender.render();

            //handler when enter key is pressed (add a new todo)
            self.listRender.on("newTodo", newTodo);

            self.listRender.on("updateTodo", updateTodo);

            self.listRender.on("delTodo", delTodo);
        });
    };

    self.stop = function() {

    };
}
