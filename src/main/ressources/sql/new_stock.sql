insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
values
(10, 1, 5.0, 'OUT', 'PCS'::unit_type, now()),
(11, 2, 2.0, 'OUT', 'PCS'::unit_type, now()),
(12, 3, 1.0, 'OUT', 'L'::unit_type, now()),
(13, 4, 4.0, 'OUT', 'L'::unit_type, now()),
