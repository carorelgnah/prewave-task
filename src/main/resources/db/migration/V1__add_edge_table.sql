create table edge
(
    id        uuid primary key not null,
    source_id varchar          not null,
    target_id varchar          not null
);

alter table edge
    add constraint unique_target unique (target_id); -- target_id is unique, so it can only belong to one source

alter table edge
    add constraint unique_source_target unique (source_id, target_id);

create index if not exists index_source_id on edge (source_id);
create index if not exists index_target_id on edge (target_id);