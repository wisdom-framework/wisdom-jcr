package todo.models;

import org.jcrom.annotations.*;

import java.util.ArrayList;
import java.util.List;

@JcrNode(nodeType = "sample:todolist")
public class TodoList {

    @JcrPath
    private String path;

    @JcrName
    private String id;

    @JcrProperty
    private String name;

    @JcrProperty
    private String owner;

    @JcrChildNode(createContainerNode = false)
    private List<Todo> todos = new ArrayList<Todo>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
