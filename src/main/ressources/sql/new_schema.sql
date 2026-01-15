create type unit_type as enum ('PCS', 'KG', 'L');

create table dish_ingredient (
    id serial primary key,
    id_dish integer not null constraint dish_id_fk references dish(id),
    id_ingredient integer not null constraint ingredient_id_fk references ingredient(id),
    quantity_required numeric(5, 2) not null,
    unit unit_type not null
);

alter table ingredient drop column id_dish;

alter table dish rename price to selling_price;

insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
values (1, 1, 1, 0.20, 'KG'::unit_type),
    (2, 1, 2, 0.15, 'KG'::unit_type),
    (3, 2, 3, 1.00, 'KG'::unit_type),
    (4, 4, 4, 0.30, 'KG'::unit_type),
    (5, 4, 5, 0.20, 'KG'::unit_type);

alter table dish alter column selling_price type numeric(10,2);

update dish_ingredient set quantity_required = 0.20 where id = 1;
update dish_ingredient set quantity_required = 0.15 where id = 2;
update dish_ingredient set quantity_required = 1.00 where id = 3;
update dish_ingredient set quantity_required = 0.30 where id = 4;
update dish_ingredient set quantity_required = 0.20 where id = 5;

update dish set selling_price = 3500.00 where id = 1;
update dish set selling_price = 12000.00 where id = 2;
update dish set selling_price = null where id = 3;
update dish set selling_price = 8000.00 where id = 4;
update dish set selling_price = null where id = 5;
