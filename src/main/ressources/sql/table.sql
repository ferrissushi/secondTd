create table if not exists "table" (
    id serial primary key,
    number integer not null
);

alter table "order" add column if not exists id_table integer references "table"(id);

alter table "order" add column if not exists arrival_datetime timestamp;

alter table "order" add column if not exists departure_datetime timestamp;

alter table "order"
add constraint order_arrival_before_departure
check (arrival_datetime < departure_datetime);

insert into "table" (id, number) values (1, 1), (2, 2), (3, 3), (4, 4), (5, 5);
