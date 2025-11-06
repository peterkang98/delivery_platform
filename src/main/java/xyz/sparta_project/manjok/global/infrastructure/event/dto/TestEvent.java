package xyz.sparta_project.manjok.global.infrastructure.event.dto;

public class TestEvent {
    private String data;

    public TestEvent() {}

    public TestEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
