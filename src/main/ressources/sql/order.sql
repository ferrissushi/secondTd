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

insert into "order" (id, reference, creation_datetime) values
  (1, 'ORD00001', '2024-01-01 00:00'),
  (2, 'ORD00002', '2024-01-02 00:00');

insert into dish_order (id, id_order, id_dish, quantity) values
    (1, 1, 1, 2), (2, 1, 2, 2), (1, 2, 3, 1);
