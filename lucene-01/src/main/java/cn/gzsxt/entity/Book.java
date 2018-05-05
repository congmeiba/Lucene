package cn.gzsxt.entity;

import java.io.Serializable;

public class Book implements Serializable {

    private Integer id;

    private String name;

    private Float price;

    private String pic;

    private String description;

    public Book() {
        super();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", pic='" + pic + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public Float getPrice() {
        return price;
    }

    public String getPic() {
        return pic;
    }

    public String getDescription() {
        return description;
    }
}
