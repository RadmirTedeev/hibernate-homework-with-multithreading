drop table if exists items;

create table items (id serial primary key, val int, version bigint default 0);

insert into items (val) values 
	(0), (0), (0), (0), (0), (0), (0), (0), (0), (0),
	(0), (0), (0), (0), (0), (0), (0), (0), (0), (0),
	(0), (0), (0), (0), (0), (0), (0), (0), (0), (0),
	(0), (0), (0), (0), (0), (0), (0), (0), (0), (0);