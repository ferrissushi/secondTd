create type mouvement_type as enum ('IN', 'OUT');

create table if not exists stock_movement (
    id serial primary key,
    id_ingredient integer constraint fk_ingredient references ingredient(id),
    quantity numeric(10,2),
    type mouvement_type,
    unit unit_type,
    creation_datetime timestamp
)
