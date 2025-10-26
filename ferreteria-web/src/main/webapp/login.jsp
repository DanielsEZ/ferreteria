<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Iniciar sesión</title>
  <style>
    body{display:flex; align-items:center; justify-content:center; height:100vh; font-family:Arial, sans-serif; background:#f5f6f8}
    .card{width:360px; background:#fff; padding:2rem; border-radius:10px; box-shadow:0 10px 30px rgba(0,0,0,.06)}
    input{width:100%; padding:.6rem .7rem; margin:.3rem 0 1rem; border:1px solid #ccc; border-radius:6px}
    button{width:100%; padding:.6rem .7rem; border:none; background:#0b5ed7; color:#fff; border-radius:6px; cursor:pointer}
    .error{color:#b00020; margin:.5rem 0}
  </style>
</head>
<body>
  <div class="card">
    <h2 style="margin-top:0">Ferretería - Iniciar sesión</h2>
    <% if (error != null) { %>
      <div class="error"><%= error %></div>
    <% } %>
    <form method="post" action="<%= request.getContextPath() %>/auth/login">
      <label>Usuario</label>
      <input type="text" name="username" required />
      <label>Contraseña</label>
      <input type="password" name="password" required />
      <button type="submit">Entrar</button>
    </form>
  </div>
</body>
</html>
