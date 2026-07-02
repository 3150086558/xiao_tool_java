const $ = (id) => document.getElementById(id);
const fmt = (n) => `¥${Number(n || 0).toFixed(2)}`;
let records = [];
let currentUser = null;

function today() { return new Date().toISOString().slice(0, 10); }
function currentMonth() { return new Date().toISOString().slice(0, 7); }
function query() {
    const params = new URLSearchParams();
    if ($('monthFilter').value) params.set('month', $('monthFilter').value);
    if ($('typeFilter').value) params.set('type', $('typeFilter').value);
    if ($('keywordFilter').value.trim()) params.set('keyword', $('keywordFilter').value.trim());
    return params.toString();
}

async function api(path, options = {}) {
    const res = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options });
    let data;
    try {
        data = await res.json();
    } catch (e) {
        if (!res.ok) throw new Error(`请求失败 (HTTP ${res.status})`);
        throw new Error('响应解析失败');
    }
    if (!res.ok) throw new Error(data.error || data.message || `请求失败 (HTTP ${res.status})`);
    return data;
}

// 页面名称到ID的映射
const PAGE_MAP = {
    'home': 'homePage',
    'accounting': 'accountingPage',
    'todo': 'todoPage',
    'notes': 'notesPage',
    'stats': 'statsPage',
    'userMgmt': 'userMgmtPage',
    'dbQuery': 'dbQueryPage'
};

// 页面导航
function showPage(pageName) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));
    
    const pageId = PAGE_MAP[pageName];
    if (pageId && $(pageId)) {
        $(pageId).classList.add('active');
    }
    
    // 加载对应页面数据
    if (pageName === 'accounting') load();
    else if (pageName === 'todo') loadTodos();
    else if (pageName === 'notes') loadNotes();
    else if (pageName === 'stats') loadStats();
    else if (pageName === 'home') load();
    else if (pageName === 'userMgmt') loadUsers();
    else if (pageName === 'dbQuery') loadDbQuery();
}

// 渲染菜单
function renderMenus(menus) {
    const container = $('menuContainer');
    let html = '';
    
    // 首页菜单项
    html += `
        <div class="menu-item active" onclick="showPage('home'); highlightMenu(this);">
            <span class="menu-item-icon">🏠</span>
            <span class="menu-item-text">首页</span>
        </div>
    `;
    
    menus.forEach(menu => {
        // 系统管理菜单只对admin可见
        if (menu.name === '系统管理' && currentUser && currentUser.username !== 'admin') return;
        
        html += `<div class="menu-group">`;
        html += `<div class="menu-group-title">${menu.name}</div>`;
        
        if (menu.children && menu.children.length > 0) {
            html += `<div class="submenu">`;
            menu.children.forEach(child => {
                // 所有二级菜单都可点击
                const pageName = child.name === '记账' ? 'accounting' :
                                 child.name === '统计报表' ? 'stats' :
                                 child.name === '待办事项' ? 'todo' :
                                 child.name === '备忘录' ? 'notes' :
                                 child.name === '用户管理' ? 'userMgmt' :
                                 child.name === '数据库查询' ? 'dbQuery' : '';
                html += `
                    <div class="menu-item" onclick="showPage('${pageName}'); highlightMenu(this);">
                        <span class="menu-item-icon">${child.icon}</span>
                        <span class="menu-item-text">${child.name}</span>
                    </div>
                `;
            });
            html += `</div>`;
        }
        
        html += `</div>`;
    });
    
    container.innerHTML = html;
}

function highlightMenu(el) {
    document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));
    el.classList.add('active');
}

async function load() {
    const q = query();
    const [list, summary] = await Promise.all([
        api(`/api/records?${q}`),
        api(`/api/summary?${q}`)
    ]);
    records = list.records;
    renderTable();
    renderSummary(summary);
    renderDashboard(summary);
}

function renderDashboard(summary) {
    // 更新仪表盘数据
    $('dashboardIncome').textContent = fmt(summary.income);
    $('dashboardExpense').textContent = fmt(summary.expense);
    $('dashboardBalance').textContent = fmt(summary.balance);
    
    // 显示最近5条记录
    const recentContainer = $('recentRecords');
    if (records.length === 0) {
        recentContainer.innerHTML = '<div class="empty-state">暂无记录</div>';
    } else {
        const recentRecords = records.slice(0, 5);
        recentContainer.innerHTML = recentRecords.map(r => `
            <div class="recent-item">
                <div class="recent-info">
                    <span class="recent-category">${escapeHtml(r.category)}</span>
                    <span class="recent-date">${r.record_date}</span>
                </div>
                <span class="recent-amount ${r.type}">${r.type === 'income' ? '+' : '-'}${fmt(r.amount)}</span>
            </div>
        `).join('');
    }
}

function renderSummary(s) {
    $('income').textContent = fmt(s.income);
    $('expense').textContent = fmt(s.expense);
    $('balance').textContent = fmt(s.balance);
    const box = $('categorySummary');
    if (!s.categories.length) { box.innerHTML = '<div class="empty">暂无汇总数据</div>'; return; }
    box.innerHTML = s.categories.map(x => `
        <div class="summaryItem">
            <span>${x.type === 'income' ? '收入' : '支出'} · ${escapeHtml(x.category)}</span>
            <strong>${fmt(x.amount)}</strong>
        </div>`).join('');
}

function renderTable() {
    const body = $('recordBody');
    if (!records.length) {
        body.innerHTML = '<tr><td colspan="8" class="empty">暂无账单，先新增一条吧</td></tr>';
        return;
    }
    body.innerHTML = records.map(r => `
        <tr>
            <td>${r.record_date}</td>
            <td><span class="tag ${r.type}">${r.type === 'income' ? '收入' : '支出'}</span></td>
            <td>${escapeHtml(r.category)}</td>
            <td>${fmt(r.amount)}</td>
            <td>${escapeHtml(r.sub_category || '')}</td>
            <td>${escapeHtml(r.account || '')}</td>
            <td class="note" title="${escapeHtml(r.note || '')}">${escapeHtml(r.note || '')}</td>
            <td><div class="rowActions"><button onclick="editRecord(${r.id})">编辑</button><button class="danger" onclick="deleteRecord(${r.id})">删除</button></div></td>
        </tr>`).join('');
}

function escapeHtml(v) {
    return String(v).replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
}

function formData() {
    return {
        record_date: $('recordDate').value,
        type: $('type').value,
        category: $('category').value.trim(),
        sub_category: $('subCategory').value.trim(),
        amount: $('amount').value,
        account: $('account').value.trim(),
        note: $('note').value.trim()
    };
}

function resetForm() {
    $('recordId').value = '';
    $('recordForm').reset();
    $('recordDate').value = today();
    $('type').value = 'expense';
}

window.editRecord = function(id) {
    const r = records.find(x => x.id === id);
    if (!r) return;
    $('recordId').value = r.id;
    $('recordDate').value = r.record_date;
    $('type').value = r.type;
    $('category').value = r.category;
    $('subCategory').value = r.sub_category || '';
    $('amount').value = r.amount;
    $('account').value = r.account || '';
    $('note').value = r.note || '';
    showPage('accounting');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

window.deleteRecord = async function(id) {
    if (!confirm('确定删除这条账单吗？')) return;
    await api(`/api/records/${id}`, { method: 'DELETE' });
    await load();
}

$('recordForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const id = $('recordId').value;
        if (id) await api(`/api/records/${id}`, { method: 'PUT', body: JSON.stringify(formData()) });
        else await api('/api/records', { method: 'POST', body: JSON.stringify(formData()) });
        resetForm();
        await load();
    } catch (err) { alert(err.message); }
});

$('resetBtn').addEventListener('click', resetForm);
['monthFilter','typeFilter'].forEach(id => $(id).addEventListener('change', load));
$('keywordFilter').addEventListener('input', () => { clearTimeout(window.__kw); window.__kw = setTimeout(load, 300); });

$('exportExcelBtn').addEventListener('click', () => { window.location.href = `/api/export.xlsx?${query()}`; });
$('exportCsvBtn').addEventListener('click', () => { window.location.href = `/api/export.csv?${query()}`; });

// 删除全部
$('clearAllBtn').addEventListener('click', async () => {
    if (!confirm('⚠️ 警告：此操作将永久删除你的所有记账记录，不可恢复！\n\n确定要继续吗？')) return;
    if (!confirm('再次确认：你真的要删除所有数据吗？')) return;
    try {
        await api('/api/clear-all', { method: 'DELETE' });
        alert('✅ 所有数据已删除');
        await load();
    } catch (err) { alert('删除失败：' + err.message); }
});

// 导入功能
let selectedFile = null;

$('importBtn').addEventListener('click', () => {
    clearSelectedFile();
    $('importModal').style.display = 'flex';
});

// 下载模板
$('downloadTemplateBtn').addEventListener('click', () => {
    window.location.href = '/api/download-template';
});

// 点击上传区域选择文件
$('uploadArea').addEventListener('click', () => {
    $('fileInput').click();
});

// 拖拽上传
$('uploadArea').addEventListener('dragover', (e) => {
    e.preventDefault();
    $('uploadArea').classList.add('drag-over');
});
$('uploadArea').addEventListener('dragleave', () => {
    $('uploadArea').classList.remove('drag-over');
});
$('uploadArea').addEventListener('drop', (e) => {
    e.preventDefault();
    $('uploadArea').classList.remove('drag-over');
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        handleFileSelect(files[0]);
    }
});

// 选择文件
$('fileInput').addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        handleFileSelect(e.target.files[0]);
    }
});

// 处理文件选择
function handleFileSelect(file) {
    if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
        alert('请选择 Excel 文件（.xlsx 或 .xls 格式）');
        return;
    }
    selectedFile = file;
    $('selectedFileName').textContent = file.name;
    $('selectedFile').style.display = 'block';
    $('confirmImportBtn').style.display = 'block';
}

// 清除已选文件
function clearSelectedFile() {
    selectedFile = null;
    $('fileInput').value = '';
    $('selectedFile').style.display = 'none';
    const btn = $('confirmImportBtn');
    btn.style.display = 'none';
    btn.disabled = false;
    btn.textContent = '确认导入';
    btn.style.opacity = '';
    btn.style.cursor = '';
    $('importResult').innerHTML = '';
    $('importProgressWrap').style.display = 'none';
    $('importProgressBar').style.width = '0%';
    $('importProgressPercent').textContent = '0%';
    $('importProgressLabel').textContent = '正在导入...';
}

// 确认导入
$('confirmImportBtn').addEventListener('click', async () => {
    if (!selectedFile) return;

    const btn = $('confirmImportBtn');
    btn.disabled = true;
    btn.textContent = '导入中...';
    $('importResult').innerHTML = '';
    $('importProgressWrap').style.display = 'block';

    // 模拟进度条
    let progress = 0;
    const progressBar = $('importProgressBar');
    const progressPercent = $('importProgressPercent');
    const progressLabel = $('importProgressLabel');
    const progressInterval = setInterval(() => {
        if (progress < 90) {
            progress += Math.random() * 8;
            if (progress > 90) progress = 90;
            progressBar.style.width = progress + '%';
            progressPercent.textContent = Math.floor(progress) + '%';
        }
    }, 300);

    try {
        const formDataObj = new FormData();
        formDataObj.append('file', selectedFile);
        const res = await fetch('/api/import', {
            method: 'POST',
            body: formDataObj
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || '导入失败');

        clearInterval(progressInterval);
        progress = 100;
        progressBar.style.width = '100%';
        progressPercent.textContent = '100%';
        progressLabel.textContent = '导入完成';

        // 显示结果
        let html = '';
        if (data.success > 0 && (!data.errors || data.errors.length === 0)) {
            html += `
                <div class="success-message">
                    🎉 导入成功！共导入 ${data.success} 条记录
                </div>
            `;
        } else {
            if (data.errors && data.errors.length) {
                html += `
                    <div style="text-align: center; padding: 20px; background: #fffbeb; border-radius: 12px; border: 2px solid #f59e0b; margin-bottom: 15px;">
                        <div style="font-size: 18px; font-weight: bold; color: #d97706; margin-bottom: 8px;">导入完成，有部分错误</div>
                        <div style="color: #78350f;">成功导入 <strong>${data.success}</strong> 条，失败 <strong>${data.errors.length}</strong> 条</div>
                    </div>
                `;
                html += '<ul style="color: #dc2626; max-height: 120px; overflow-y: auto; margin: 0; padding-left: 20px; background: #fef2f2; border-radius: 8px; padding: 12px 12px 12px 32px;">';
                html += data.errors.map(e => `<li style="margin-bottom: 4px;">${escapeHtml(e)}</li>`).join('');
                html += '</ul>';
            } else {
                html += `<div class="success-message">✅ 导入成功！共导入 ${data.success} 条记录</div>`;
            }
        }
        $('importResult').innerHTML = html;
        // 导入完成后禁用按钮
        btn.textContent = '已导入';
        btn.disabled = true;
        btn.style.opacity = '0.6';
        btn.style.cursor = 'not-allowed';
        await load();
    } catch (err) {
        clearInterval(progressInterval);
        progressBar.style.width = '0%';
        progressPercent.textContent = '0%';
        $('importProgressWrap').style.display = 'none';
        $('importResult').innerHTML = `
            <div class="error-message">
                ❌ 导入失败：${escapeHtml(err.message)}
            </div>
        `;
        // 失败时恢复按钮
        btn.disabled = false;
        btn.textContent = '确认导入';
        btn.style.opacity = '';
        btn.style.cursor = '';
    }
});

// 修改密码功能
$('changePasswordBtn').addEventListener('click', () => {
    $('changePasswordModal').style.display = 'flex';
    $('changePasswordForm').reset();
    $('changePasswordResult').innerHTML = '';
});

$('changePasswordForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const oldPassword = $('oldPassword').value;
    const newPassword = $('newPassword').value;
    const confirmPassword = $('confirmNewPassword').value;
    
    if (newPassword !== confirmPassword) {
        $('changePasswordResult').innerHTML = `
            <div class="error-message">两次输入的新密码不一致</div>
        `;
        return;
    }
    
    try {
        await api('/api/change-password', {
            method: 'POST',
            body: JSON.stringify({
                old_password: oldPassword,
                new_password: newPassword,
                confirm_password: confirmPassword
            })
        });
        
        $('changePasswordResult').innerHTML = `
            <div class="success-message">✅ 密码修改成功</div>
        `;
        setTimeout(() => {
            $('changePasswordModal').style.display = 'none';
        }, 1500);
    } catch (err) {
        $('changePasswordResult').innerHTML = `
            <div class="error-message">❌ ${escapeHtml(err.message)}</div>
        `;
    }
});

// 退出登录
$('logoutBtn').addEventListener('click', async () => {
    if (confirm('确定要退出登录吗？')) {
        try {
            await api('/api/logout', { method: 'POST' });
            window.location.href = 'login.html';
        } catch (err) {
            alert('退出失败：' + err.message);
        }
    }
});

// ========== 待办事项 ==========
async function loadTodos() {
    try {
        const data = await api('/api/todos');
        renderTodos(data.todos);
    } catch (e) {
        console.error('加载待办事项失败:', e);
    }
}

function renderTodos(todos) {
    const container = $('todoList');
    if (!todos.length) {
        container.innerHTML = '<div class="empty-state">暂无待办事项，快来添加吧</div>';
        return;
    }
    container.innerHTML = todos.map(t => `
        <div class="todo-item ${t.completed ? 'completed' : ''}">
            <input type="checkbox" ${t.completed ? 'checked' : ''} 
                   onchange="toggleTodo(${t.id}, this.checked)" />
            <span class="todo-title">${escapeHtml(t.title)}</span>
            ${t.due_date ? `<span class="todo-due">📅 ${t.due_date}</span>` : ''}
            <button class="danger" onclick="deleteTodo(${t.id})">删除</button>
        </div>
    `).join('');
}

window.addTodo = async function() {
    const input = $('todoInput');
    const title = input.value.trim();
    if (!title) return;
    
    try {
        await api('/api/todos', {
            method: 'POST',
            body: JSON.stringify({ title, priority: 0 })
        });
        input.value = '';
        loadTodos();
    } catch (e) {
        alert('添加失败：' + e.message);
    }
};

window.toggleTodo = async function(id, completed) {
    try {
        await api(`/api/todos/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ completed: completed ? 1 : 0 })
        });
        loadTodos();
    } catch (e) {
        console.error('更新失败:', e);
    }
};

window.deleteTodo = async function(id) {
    if (!confirm('确定删除这个待办事项吗？')) return;
    try {
        await api(`/api/todos/${id}`, { method: 'DELETE' });
        loadTodos();
    } catch (e) {
        alert('删除失败：' + e.message);
    }
};

// ========== 备忘录 ==========
async function loadNotes() {
    try {
        const data = await api('/api/notes');
        renderNotes(data.notes);
    } catch (e) {
        console.error('加载备忘录失败:', e);
    }
}

function renderNotes(notes) {
    const container = $('noteList');
    if (!notes.length) {
        container.innerHTML = '<div class="empty-state">暂无备忘录，快来记录吧</div>';
        return;
    }
    container.innerHTML = notes.map(n => `
        <div class="note-item">
            <div class="note-header">
                <h4 class="note-title">${escapeHtml(n.title)}</h4>
                <button class="danger" onclick="deleteNote(${n.id})">删除</button>
            </div>
            <div class="note-content">${escapeHtml(n.content || '(无内容)')}</div>
            <div class="note-time">${n.updated_at}</div>
        </div>
    `).join('');
}

window.addNote = async function() {
    const titleInput = $('noteTitleInput');
    const title = titleInput.value.trim();
    if (!title) return;
    
    try {
        await api('/api/notes', {
            method: 'POST',
            body: JSON.stringify({ title, content: '' })
        });
        titleInput.value = '';
        loadNotes();
    } catch (e) {
        alert('添加失败：' + e.message);
    }
};

window.deleteNote = async function(id) {
    if (!confirm('确定删除这个备忘录吗？')) return;
    try {
        await api(`/api/notes/${id}`, { method: 'DELETE' });
        loadNotes();
    } catch (e) {
        alert('删除失败：' + e.message);
    }
};

// ========== 统计报表 ==========
async function loadStats() {
    try {
        const q = query();
        const summary = await api(`/api/summary?${q}`);
        $('statsIncome').textContent = fmt(summary.income);
        $('statsExpense').textContent = fmt(summary.expense);
        $('statsBalance').textContent = fmt(summary.balance);
        
        const box = $('statsCategories');
        if (!summary.categories.length) {
            box.innerHTML = '<div class="empty">暂无数据</div>';
            return;
        }
        box.innerHTML = summary.categories.map(x => `
            <div class="summaryItem">
                <span>${x.type === 'income' ? '收入' : '支出'} · ${escapeHtml(x.category)}</span>
                <strong>${fmt(x.amount)}</strong>
            </div>
        `).join('');
    } catch (e) {
        console.error('加载统计数据失败:', e);
    }
}

// ========== 用户管理 ==========
async function loadUsers() {
    try {
        const data = await api('/api/users');
        const body = $('userListBody');
        if (!data.users || data.users.length === 0) {
            body.innerHTML = '<tr><td colspan="7" class="empty">暂无用户数据</td></tr>';
            return;
        }
        body.innerHTML = data.users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${escapeHtml(u.username)}</strong></td>
                <td>${u.created_at}</td>
                <td>${u.record_count || 0}</td>
                <td>${u.todo_count || 0}</td>
                <td>${u.note_count || 0}</td>
                <td>
                    <div class="rowActions">
                        <button onclick="resetUserPassword(${u.id}, '${escapeHtml(u.username)}')">重置密码</button>
                        ${u.username !== 'admin' ? `<button class="danger" onclick="deleteUser(${u.id}, '${escapeHtml(u.username)}')">删除</button>` : ''}
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        if (e.message.includes('仅管理员')) {
            $('userListBody').innerHTML = '<tr><td colspan="7" class="empty">仅管理员可访问此页面</td></tr>';
        } else {
            $('userListBody').innerHTML = '<tr><td colspan="7" class="empty">加载失败</td></tr>';
        }
    }
}

window.resetUserPassword = async function(userId, username) {
    const newPwd = prompt(`请输入为用户 "${username}" 设置的新密码：\n\n密码规则：8-20位，包含大小写字母和数字`);
    if (!newPwd) return;
    try {
        await api('/api/users/reset-password', {
            method: 'PUT',
            body: JSON.stringify({ user_id: userId, new_password: newPwd })
        });
        alert(`用户 "${username}" 的密码已重置成功`);
    } catch (e) {
        alert('重置失败：' + e.message);
    }
};

window.deleteUser = async function(userId, username) {
    if (!confirm(`确定要删除用户 "${username}" 吗？\n该用户的所有数据（记账、待办、备忘录）将被永久删除！`)) return;
    try {
        await api(`/api/users/${userId}`, { method: 'DELETE' });
        alert(`用户 "${username}" 已删除`);
        loadUsers();
    } catch (e) {
        alert('删除失败：' + e.message);
    }
};

// ========== 数据库查询 ==========
let savedDbConnections = [];
let activeDbConn = null; // 当前选中的连接

function loadDbQuery() {
    loadDbConnections();
}

async function loadDbConnections() {
    try {
        const data = await api('/api/db-connections');
        savedDbConnections = data.connections || [];
        renderDbConnections();
    } catch (e) {
        $('dbSavedConns').innerHTML = '<div class="empty">加载失败</div>';
    }
}

function renderDbConnections() {
    const box = $('dbSavedConns');
    if (savedDbConnections.length === 0) {
        box.innerHTML = '<div class="empty">暂无保存的连接，点击"新增连接"开始配置</div>';
        $('dbActionsPanel').style.display = 'none';
        return;
    }
    const typeBadge = { mysql: '#00758f', postgres: '#336791', sqlite: '#44a058' };
    const typeLabel = { mysql: 'MySQL', postgres: 'PostgreSQL', sqlite: 'SQLite' };
    box.innerHTML = savedDbConnections.map(c => `
        <div class="db-conn-card ${activeDbConn && activeDbConn.id === c.id ? 'active' : ''}" onclick="selectDbConnection(${c.id})">
            <div class="db-conn-info">
                <strong>${escapeHtml(c.name)}</strong>
                <span class="db-type-badge" style="background:${typeBadge[c.db_type] || '#666'}">${typeLabel[c.db_type] || c.db_type}</span>
                <span style="color:#999; font-size:13px">${c.db_type === 'sqlite' ? escapeHtml(c.sqlite_path || '') : escapeHtml(c.host + ':' + c.port + '/' + c.database)}</span>
            </div>
            <div class="db-conn-actions">
                <button onclick="event.stopPropagation(); selectDbConnection(${c.id})">使用</button>
                <button onclick="event.stopPropagation(); editDbConnection(${c.id})">编辑</button>
                <button class="danger" onclick="event.stopPropagation(); deleteDbConnection(${c.id}, '${escapeHtml(c.name)}')">删除</button>
            </div>
        </div>
    `).join('');
}

window.selectDbConnection = async function(connId) {
    activeDbConn = savedDbConnections.find(c => c.id === connId);
    if (!activeDbConn) return;
    renderDbConnections();
    // 填充表单
    $('dbType').value = activeDbConn.db_type;
    $('dbHost').value = activeDbConn.host || '';
    $('dbPort').value = activeDbConn.port || '';
    $('dbUsername').value = activeDbConn.username || '';
    $('dbPassword').value = '';
    $('dbDatabase').value = activeDbConn.database || '';
    $('dbSqlitePath').value = activeDbConn.sqlite_path || '';
    onDbTypeChange();
    $('dbFormPanel').style.display = 'none';
    $('dbActionsPanel').style.display = 'block';
    // 隐藏之前的结果
    $('dbConnectResult').innerHTML = '';
    $('dbTablesPanel').style.display = 'none';
    $('dbSchemaPanel').style.display = 'none';
};

window.toggleDbForm = function() {
    const panel = $('dbFormPanel');
    const isHidden = panel.style.display === 'none';
    panel.style.display = isHidden ? 'block' : 'none';
    if (isHidden) {
        $('dbFormTitle').textContent = '新增数据库连接';
        $('dbConnName').value = '';
        $('dbType').value = 'mysql';
        $('dbHost').value = '127.0.0.1';
        $('dbPort').value = '3306';
        $('dbUsername').value = '';
        $('dbPassword').value = '';
        $('dbDatabase').value = '';
        $('dbSqlitePath').value = '';
        $('dbSaveResult').innerHTML = '';
        onDbTypeChange();
    }
};

window.cancelDbForm = function() {
    $('dbFormPanel').style.display = 'none';
    editingDbConnId = null;
    $('dbFormTitle').textContent = '新增数据库连接';
    $('saveDbConnBtn').textContent = '保存连接';
};

let editingDbConnId = null;

window.editDbConnection = function(connId) {
    const c = savedDbConnections.find(x => x.id === connId);
    if (!c) return;
    editingDbConnId = connId;
    $('dbFormTitle').textContent = '编辑数据库连接';
    $('saveDbConnBtn').textContent = '更新连接';
    $('dbConnName').value = c.name;
    $('dbType').value = c.db_type;
    $('dbHost').value = c.host || '';
    $('dbPort').value = c.port || '';
    $('dbUsername').value = c.username || '';
    $('dbPassword').value = '';
    $('dbDatabase').value = c.database || '';
    $('dbSqlitePath').value = c.sqlite_path || '';
    $('dbSaveResult').innerHTML = '';
    onDbTypeChange();
    $('dbFormPanel').style.display = 'block';
    // 滚动到表单
    $('dbFormPanel').scrollIntoView({ behavior: 'smooth', block: 'center' });
};

window.onDbTypeChange = function() {
    const isSqlite = $('dbType').value === 'sqlite';
    document.querySelectorAll('.db-field-net').forEach(el => el.style.display = isSqlite ? 'none' : '');
    $('dbSqliteLabel').style.display = isSqlite ? '' : 'none';
    // 只在端口为空时才填充默认值，避免覆盖用户已填写的端口
    if (isSqlite) {
        if (!$('dbPort').value) $('dbPort').value = '';
    } else {
        if (!$('dbPort').value) {
            $('dbPort').value = $('dbType').value === 'postgres' ? '5432' : '3306';
        }
    }
};

function getActiveConfig() {
    const dbType = $('dbType').value;
    const config = {
        db_type: dbType,
        host: $('dbHost').value.trim() || '127.0.0.1',
        port: parseInt($('dbPort').value.trim() || (dbType === 'postgres' ? 5432 : 3306)),
        username: $('dbUsername').value.trim(),
        password: $('dbPassword').value,
        database: $('dbDatabase').value.trim(),
        sqlite_path: $('dbSqlitePath').value.trim()
    };
    // 如果有选中的连接且密码为空，用保存的密码
    if (activeDbConn && !config.password) {
        config.password = activeDbConn.password || '';
    }
    return config;
}

window.saveDbConnection = async function() {
    const name = $('dbConnName').value.trim();
    const dbType = $('dbType').value;
    if (!name) { $('dbSaveResult').innerHTML = '<div class="error-message">请输入连接名称</div>'; return; }
    if (dbType === 'sqlite') {
        if (!$('dbSqlitePath').value.trim()) { $('dbSaveResult').innerHTML = '<div class="error-message">请输入 SQLite 文件路径</div>'; return; }
    } else {
        if (!$('dbUsername').value.trim() || !$('dbDatabase').value.trim()) { $('dbSaveResult').innerHTML = '<div class="error-message">请输入用户名和数据库名</div>'; return; }
    }
    const payload = {
        name, db_type: dbType,
        host: $('dbHost').value.trim(),
        port: parseInt($('dbPort').value.trim() || 0),
        username: $('dbUsername').value.trim(),
        password: $('dbPassword').value,
        database: $('dbDatabase').value.trim(),
        sqlite_path: $('dbSqlitePath').value.trim()
    };
    try {
        if (editingDbConnId) {
            await api(`/api/db-connections/${editingDbConnId}`, { method: 'PUT', body: JSON.stringify(payload) });
            $('dbSaveResult').innerHTML = '<div class="success-message">连接已更新</div>';
            editingDbConnId = null;
        } else {
            await api('/api/db-connections', { method: 'POST', body: JSON.stringify(payload) });
            $('dbSaveResult').innerHTML = '<div class="success-message">连接配置已保存</div>';
        }
        $('dbFormPanel').style.display = 'none';
        $('dbFormTitle').textContent = '新增数据库连接';
        $('saveDbConnBtn').textContent = '保存连接';
        await loadDbConnections();
    } catch (e) {
        $('dbSaveResult').innerHTML = `<div class="error-message">${escapeHtml(e.message)}</div>`;
    }
};

window.deleteDbConnection = async function(connId, name) {
    if (!confirm(`确定删除连接 "${name}" 吗？`)) return;
    try {
        await api(`/api/db-connections/${connId}`, { method: 'DELETE' });
        if (activeDbConn && activeDbConn.id === connId) {
            activeDbConn = null;
            $('dbActionsPanel').style.display = 'none';
            $('dbTablesPanel').style.display = 'none';
            $('dbSchemaPanel').style.display = 'none';
        }
        await loadDbConnections();
    } catch (e) { alert('删除失败：' + e.message); }
};

window.quickTestConnection = async function() {
    const config = getActiveConfig();
    // 构建连接信息展示
    let infoHtml = '';
    if (config.db_type === 'sqlite') {
        infoHtml = `<div style="margin-bottom:10px;font-size:13px;color:#666">正在连接 SQLite：${escapeHtml(config.sqlite_path)}</div>`;
    } else {
        const pwdDisplay = config.password ? '•'.repeat(Math.min(config.password.length, 8)) : '(空)';
        infoHtml = `<div style="margin-bottom:10px;font-size:13px;color:#666">
            正在连接 ${config.db_type === 'mysql' ? 'MySQL' : 'PostgreSQL'}：
            ${escapeHtml(config.host)}:${config.port} / 用户：${escapeHtml(config.username)} / 密码：${pwdDisplay} / 数据库：${escapeHtml(config.database)}
        </div>`;
    }
    $('dbConnectResult').innerHTML = infoHtml + '<div style="color: #666">连接中...</div>';
    try {
        await api('/api/db-query', { method: 'POST', body: JSON.stringify({ action: 'connect', config }) });
        $('dbConnectResult').innerHTML = infoHtml + '<div class="success-message">✅ 连接成功</div>';
    } catch (e) {
        $('dbConnectResult').innerHTML = infoHtml + `<div class="error-message">❌ 连接失败：${escapeHtml(e.message)}</div>`;
    }
};

window.quickLoadTables = async function() {
    const config = getActiveConfig();
    $('dbConnectResult').innerHTML = '';
    try {
        const data = await api('/api/db-query', { method: 'POST', body: JSON.stringify({ action: 'tables', config }) });
        const box = $('dbTablesList');
        if (!data.tables || data.tables.length === 0) {
            box.innerHTML = '<div class="empty">暂无表</div>';
        } else {
            box.innerHTML = data.tables.map(t => `
                <div class="db-table-tag" onclick="loadDbSchema('${escapeHtml(t)}')">${escapeHtml(t)}</div>
            `).join('');
        }
        $('dbTablesPanel').style.display = 'block';
        $('dbSchemaPanel').style.display = 'none';
    } catch (e) {
        $('dbConnectResult').innerHTML = `<div class="error-message">❌ ${escapeHtml(e.message)}</div>`;
    }
};

window.loadDbSchema = async function(tableName) {
    const config = getActiveConfig();
    try {
        const data = await api('/api/db-query', { method: 'POST', body: JSON.stringify({ action: 'schema', table: tableName, config }) });
        $('dbSchemaTableName').textContent = tableName;
        let html = '<table><thead><tr><th>列名</th><th>类型</th><th>可空</th><th>默认值</th></tr></thead><tbody>';
        html += data.columns.map(c => `<tr><td><strong>${escapeHtml(c.name)}</strong></td><td>${escapeHtml(c.type)}</td><td>${c.nullable === 'YES' ? '是' : '否'}</td><td>${escapeHtml(c.default || '-')}</td></tr>`).join('');
        html += '</tbody></table>';
        $('dbSchemaResult').innerHTML = html;
        $('dbSchemaPanel').style.display = 'block';
    } catch (e) { alert('获取表结构失败：' + e.message); }
};

window.executeDbQuery = async function() {
    const sql = $('dbSqlInput').value.trim();
    if (!sql) { $('dbQueryResult').innerHTML = '<div class="error-message">请输入 SQL 语句</div>'; return; }
    if (!activeDbConn) { $('dbQueryResult').innerHTML = '<div class="error-message">请先选择一个数据库连接</div>'; return; }
    const config = getActiveConfig();
    $('dbQueryResult').innerHTML = '<div style="color: #666">查询中...</div>';
    try {
        const data = await api('/api/db-query', { method: 'POST', body: JSON.stringify({ action: 'query', sql, config }) });
        let html = `<div style="margin-bottom: 10px; color: #666; font-size: 14px">共 ${data.count} 条记录</div>`;
        if (data.columns.length > 0) {
            html += '<div class="tableWrap"><table><thead><tr>';
            html += data.columns.map(c => `<th>${escapeHtml(c)}</th>`).join('');
            html += '</tr></thead><tbody>';
            html += data.rows.map(row => `<tr>${row.map(v => `<td>${escapeHtml(String(v === null ? 'NULL' : v))}</td>`).join('')}</tr>`).join('');
            html += '</tbody></table></div>';
        }
        $('dbQueryResult').innerHTML = html;
    } catch (e) {
        $('dbQueryResult').innerHTML = `<div class="error-message">❌ 查询失败：${escapeHtml(e.message)}</div>`;
    }
};

// 初始化
(async function init() {
    // 检查登录状态
    try {
        const result = await api('/api/user');
        currentUser = result.user;
    } catch (e) {
        window.location.href = 'login.html';
        return;
    }
    
    // 加载菜单
    const menuResult = await api('/api/menus');
    renderMenus(menuResult.menus);
    
    // 初始化表单
    resetForm();
    $('monthFilter').value = currentMonth();
    load();
})();
