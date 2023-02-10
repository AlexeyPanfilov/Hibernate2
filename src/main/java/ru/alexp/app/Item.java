package ru.alexp.app;

import javax.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "val")
    private int val;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public Item() {
    }

    public Item(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "Item: " + id + ". " + val;
    }
}
