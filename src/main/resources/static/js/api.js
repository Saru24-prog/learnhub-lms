const TOKEN_KEY = "lh_token";
const USER_KEY = "lh_user";

const auth = {
  save(data) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify({ fullName: data.fullName, email: data.email, role: data.role }));
  },
  token() { return localStorage.getItem(TOKEN_KEY); },
  user() { return JSON.parse(localStorage.getItem(USER_KEY) || "null"); },
  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    location.href = "/login.html";
  }
};

// Each role has its own portal page
function homeFor(role) {
  return { ADMIN: "/admin.html", LECTURER: "/lecturer.html", STUDENT: "/student.html" }[role] || "/login.html";
}

// Calls the backend and automatically adds "Authorization: Bearer <token>"
async function api(url, options = {}) {
  const isForm = options.body instanceof FormData;          // file upload -> browser sets Content-Type
  const headers = { ...(isForm ? {} : { "Content-Type": "application/json" }), ...(options.headers || {}) };
  if (auth.token()) headers.Authorization = "Bearer " + auth.token();

  const res = await fetch(url, { ...options, headers });
  if (res.status === 401 && auth.token()) { auth.logout(); return; }   // token expired

  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) throw { status: res.status, ...(data || {}) };
  return data;
}

// Downloads a protected file (needs the token, so a normal <a href> can't be used)
async function downloadFile(url, fileName) {
  const res = await fetch(url, { headers: { Authorization: "Bearer " + auth.token() } });
  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw { status: res.status, message: data.message || "Download failed" };
  }
  const blob = await res.blob();
  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => URL.revokeObjectURL(a.href), 2000);
}

const esc = s => String(s ?? "").replace(/[&<>"']/g, c =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

function toast(msg, type = "success") {
  let t = document.getElementById("toast");
  if (!t) { t = document.createElement("div"); t.id = "toast"; t.className = "toast"; document.body.appendChild(t); }
  t.textContent = msg;
  t.style.background = type === "error" ? "#ca3120" : "#357a32";
  t.classList.add("show");
  setTimeout(() => t.classList.remove("show"), 2500);
}
