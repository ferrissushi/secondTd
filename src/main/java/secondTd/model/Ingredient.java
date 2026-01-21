package secondTd.model;

import java.util.Objects;

public class Ingredient {
    public enum CategoryEnum {
        VEGETABLE, ANIMAL, MARINE, DAIRY, OTHER
    }
    private int id;
    private String name;
    private Double price;
    private CategoryEnum category;


    public Ingredient(Integer id,
        String name,
        Double price,
        CategoryEnum category,
        Dish dish) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient(Integer id,
        String name,
        Double price,
        CategoryEnum category,
        Dish dish,
        Double quantityRequired){

        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public Ingredient() {
    }





    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }


    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                '}';
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPrice(), getCategory());
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ingredient other = (Ingredient) obj;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (category != other.category)
            return false;
        return true;
    }
}
