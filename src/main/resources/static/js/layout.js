// Shared layout for all portals: sidebar, menu, page switching, modals, profile page
const $ = id => document.getElementById(id);
let me;
let APP_PAGES = {};

const PORTAL_NAMES = { ADMIN: "Admin Portal", LECTURER: "Lecturer Portal", STUDENT: "Student Portal" };
const COURSE_COLORS = ["#0f6cbf", "#5b6c8f", "#3f7d6e", "#8a5a83", "#b5653a", "#46708c", "#6b7a3a", "#7a5c48"];

const courseColor = id => COURSE_COLORS[id % COURSE_COLORS.length];
const initials = name => name.split(" ").filter(Boolean).map(w => w[0]).slice(0, 2).join("").toUpperCase();
const fmtDate = d => d ? new Date(d).toLocaleDateString("en-GB") : "—";
const today = () => new Date().toLocaleDateString("en-GB", { weekday: "long", day: "numeric", month: "long", year: "numeric" });
const val = id => $(id).value.trim();
const statCards = list => `<div class="stats">${list.map(([label, value]) =>
  `<div class="stat"><div class="label">${label}</div><div class="value">${value}</div></div>`).join("")}</div>`;

// Start a portal. If the logged-in user has a different role, send them to their own portal.
async function startApp(requiredRole, pages) {
  if (!auth.token()) { location.replace("/login.html"); return; }
  me = await api("/api/users/me");
  if (!me) return;
  localStorage.setItem(USER_KEY, JSON.stringify({ fullName: me.fullName, email: me.email, role: me.role }));
  if (me.role !== requiredRole) { location.replace(homeFor(me.role)); return; }

  APP_PAGES = { ...pages, profile: { label: "Profile", render: renderProfile } };

  document.body.insertAdjacentHTML("afterbegin", `
    <aside class="sidebar">
      <div class="logo"><span>🎓</span>LearnHub</div>
      <div class="portal">${PORTAL_NAMES[me.role]}</div>
      <nav class="nav">${Object.entries(APP_PAGES).filter(([, p]) => !p.hidden)
        .map(([key, p]) => `<a data-page="${key}" onclick="go('${key}')">${p.label}</a>`).join("")}</nav>
      <div class="user-box">
        <div class="person">
          <div class="avatar">${esc(initials(me.fullName))}</div>
          <div><div class="name">${esc(me.fullName)}</div><div class="role">${me.role}</div></div>
        </div>
        <button class="btn logout" onclick="auth.logout()">Logout</button>
      </div>
    </aside>`);

  window.onhashchange = route;
  route();
}

// go('courses') or go('course', 5)  ->  #courses  or  #course/5
function go(page, param) {
  const target = "#" + page + (param !== undefined ? "/" + param : "");
  if (location.hash === target) route(); else location.hash = target;
}

function route() {
  const [page, param] = location.hash.slice(1).split("/");
  const key = APP_PAGES[page] ? page : Object.keys(APP_PAGES)[0];
  const active = APP_PAGES[key].parent || key;
  document.querySelectorAll(".nav a").forEach(a => a.classList.toggle("active", a.dataset.page === active));
  closeModal();
  Promise.resolve(APP_PAGES[key].render(param)).catch(e => toast(e.message || "Something went wrong", "error"));
}

// ---------- Modal (popup form) ----------
function field(id, label, { type = "text", value = "", placeholder = "", textarea = false } = {}) {
  const input = textarea
    ? `<textarea id="${id}" rows="3" placeholder="${esc(placeholder)}">${esc(value)}</textarea>`
    : `<input id="${id}" type="${type}" value="${esc(value)}" placeholder="${esc(placeholder)}">`;
  return `<div class="field"><label>${label}</label>${input}<div class="err" id="${id}Err"></div></div>`;
}

function openModal(title, bodyHtml, onSubmit, submitLabel = "Save") {
  closeModal();
  document.body.insertAdjacentHTML("beforeend", `
    <div class="modal-bg" id="modal">
      <div class="modal">
        <h3>${title}</h3>
        <form id="modalForm" novalidate>
          ${bodyHtml}
          <div class="alert hidden" id="modalAlert"></div>
          <div class="modal-foot">
            <button type="button" class="btn btn-light" onclick="closeModal()">Cancel</button>
            <button type="submit" class="btn btn-primary">${submitLabel}</button>
          </div>
        </form>
      </div>
    </div>`);
  $("modal").onclick = e => { if (e.target.id === "modal") closeModal(); };
  const first = document.querySelector("#modalForm input, #modalForm textarea");
  if (first) first.focus();

  $("modalForm").onsubmit = async e => {
    e.preventDefault();
    document.querySelectorAll("#modalForm .err").forEach(el => el.textContent = "");
    $("modalAlert").classList.add("hidden");
    const btn = e.target.querySelector("button[type=submit]");
    btn.disabled = true;
    try {
      await onSubmit();
      closeModal();
    } catch (err) {
      if (err.errors) {
        Object.entries(err.errors).forEach(([k, v]) => { const el = $(k + "Err"); el ? el.textContent = v : showModalAlert(v); });
      } else {
        showModalAlert(err.message || "Something went wrong");
      }
      btn.disabled = false;
    }
  };
}

function showModalAlert(msg) { $("modalAlert").textContent = msg; $("modalAlert").classList.remove("hidden"); }
function closeModal() { const m = $("modal"); if (m) m.remove(); }

// ---------- Profile page (same for every role) ----------
function renderProfile() {
  $("main").innerHTML = `
    <div class="page-title"><h1>My Profile</h1><p>Your account details</p></div>
    <div class="panel"><div class="panel-body">
      <div class="person" style="margin-bottom:20px">
        <div class="avatar big">${esc(initials(me.fullName))}</div>
        <div><h2>${esc(me.fullName)}</h2><span class="badge ${me.role}">${me.role}</span></div>
      </div>
      <div class="info-row"><span>Email</span><span>${esc(me.email)}</span></div>
      <div class="info-row"><span>Role</span><span>${me.role}</span></div>
      <div class="info-row"><span>Member since</span><span>${fmtDate(me.createdAt)}</span></div>
    </div></div>`;
}
