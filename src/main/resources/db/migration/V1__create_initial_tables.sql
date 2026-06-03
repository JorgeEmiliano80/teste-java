CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    assignment VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    start_date DATE NOT NULL,
    expected_end_date DATE NOT NULL,
    actual_end_date DATE NULL,
    total_budget NUMERIC(15, 2) NOT NULL,
    description VARCHAR(2000),
    manager_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_projects_manager
        FOREIGN KEY (manager_id)
        REFERENCES members (id)
);

CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, member_id),
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
        REFERENCES projects (id),
    CONSTRAINT fk_project_members_member
        FOREIGN KEY (member_id)
        REFERENCES members (id)
);

CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_manager_id ON projects (manager_id);
CREATE INDEX idx_project_members_member_id ON project_members (member_id);
CREATE INDEX idx_members_assignment ON members (assignment);
