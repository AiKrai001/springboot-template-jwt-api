-- Seed initial RBAC data for H2(PostgreSQL mode)

-- Roles
MERGE INTO public.sys_role (role_id, role_name, role_key, status, remark)
    KEY (role_id) VALUES
    (1, 'Administrator', 'admin', '0', 'Built-in admin');
MERGE INTO public.sys_role (role_id, role_name, role_key, status, remark)
    KEY (role_id) VALUES
    (2, 'User', 'user', '0', 'Built-in user');

-- Permissions
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (1, 'User list', 'user:list', '0', '');
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (2, 'User create', 'user:create', '0', '');
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (3, 'Role list', 'role:list', '0', '');
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (4, 'Role create', 'role:create', '0', '');
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (5, 'Permission list', 'perm:list', '0', '');
MERGE INTO public.sys_permission (permission_id, perm_name, perm_key, status, remark)
    KEY (permission_id) VALUES (6, 'Permission create', 'perm:create', '0', '');

-- Users (password sha1 of '123456')
-- 123456 -> 7c4a8d09ca3762af61e59520943dc26494f8941b
MERGE INTO public.sys_user (
    user_id, dept_id, user_name, nick_name, user_type, email, phone, sex, avatar,
    password, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark
    ) KEY (user_id) VALUES
    (1, NULL, 'admin', 'Administrator', '00', NULL, NULL, NULL, NULL,
    '7c4a8d09ca3762af61e59520943dc26494f8941b', '0', '0', NULL, NULL, 'system', CURRENT_TIMESTAMP, NULL, NULL, 'seed');
MERGE INTO public.sys_user (
    user_id, dept_id, user_name, nick_name, user_type, email, phone, sex, avatar,
    password, status, del_flag, login_ip, login_date, create_by, create_time, update_by, update_time, remark
    ) KEY (user_id) VALUES
    (2, NULL, 'user1', 'User One', '00', NULL, NULL, NULL, NULL,
    '7c4a8d09ca3762af61e59520943dc26494f8941b', '0', '0', NULL, NULL, 'system', CURRENT_TIMESTAMP, NULL, NULL, 'seed');

-- User-Role mappings
MERGE INTO public.sys_user_role (user_id, role_id) KEY (user_id, role_id) VALUES (1, 1);
MERGE INTO public.sys_user_role (user_id, role_id) KEY (user_id, role_id) VALUES (2, 2);

-- Role-Permission mappings
-- admin: all perms
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 1);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 2);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 3);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 4);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 5);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (1, 6);

-- user: read-only basic perms
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (2, 1);
MERGE INTO public.sys_role_permission (role_id, permission_id) KEY (role_id, permission_id) VALUES (2, 5);
