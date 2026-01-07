alter table dish add column if not exists price integer;

update dish set price = 2000 where name = 'Salade fraiche';
update dish set price = 6000 where name = 'Poulet grille';
