create type dish_types as enum ('START', 'MAIN', 'DESSERT');

create table if not exists dish(
    id serial primary key,
    name varchar not null,
    dish_type dish_types
);

create type ingredient_category as enum ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY', 'OTHER');

create table if not exists ingredient(
    id serial primary key,
    name varchar not null unique,
    price numeric(10,2),
    category ingredient_category,
    id_dish int,
    constraint ingredient_dish_fk
        foreign key (id_dish)
        references dish(id)
)
