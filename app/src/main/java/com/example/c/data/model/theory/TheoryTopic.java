package com.example.c.data.model.theory;

import java.util.ArrayList;
import java.util.List;

public class TheoryTopic {
    public String id;
    public String title;
    public String description;
    public List<TheoryBlock> blocks;

    public TheoryTopic() {
        blocks = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TheoryBlock> getBlocks() {
        if (blocks == null) {
            blocks = new ArrayList<>();
        }
        return blocks;
    }

    public void setBlocks(List<TheoryBlock> blocks) {
        this.blocks = blocks;
    }
}
