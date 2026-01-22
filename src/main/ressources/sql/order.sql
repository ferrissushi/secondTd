create table if not exists "order" (
    id serial primary key,
    reference varchar(8) not null,
    creation_datetime timestamp default now()
)

create table if not exists dish_order (
    id serial primary key,
    id_order integer constraint fk_order references "order"(id),
    id_dish integer constraint fk_dish references dish(id),
    quantity integer not null
);