<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
  <img src="https://github.com/MoeLuoYu/velocity-force-hosts/blob/main/velocity-force-hosts.png?raw=true" width="200" height="200" alt="ForceHosts">
</p>

<div align="center">
✨ ForceHosts - 强制主机名加入服务器 ✨

ForceHosts 是一款 Velocity 代理插件，用于强制客户端使用指定的主机名连接服务器。该插件可以有效防止玩家绕过指定的域名直接使用IP地址或其他未经授权的域名连接服务器。

</div>

<p align="center">
  <a href="https://github.com/MoeLuoYu/velocity-force-hosts/blob/main/LICENSE">
    <img src="https://img.shields.io/badge/license-MIT-green" alt="license">
  </a>
  <a href="https://www.velocitypowered.com">
    <img src="https://img.shields.io/badge/Velocity-3.0.0--latest-blue?logo=Velocity" alt="velocity"/>
  </a>
  <a href="https://github.com/MoeLuoYu/velocity-force-hosts/releases">
    <img  src="https://img.shields.io/github/v/release/MoeLuoYu/velocity-force-hosts" alt="release">
  </a>
</p>
<p align="center">下载</p>
<p align="center">
  <a href="https://www.minebbs.com/resources/forcehosts.14858/">⬇️MineBBS</a>
</p>

## 功能特性

- 🚫 **ping 请求阻止** 阻止不在白名单中的主机名进行 ping 请求
- ⛔ **连接阻止** 阻止不在白名单中的主机名进行连接
- 📢 **防压测** 支持防频繁刷新功能，防止恶意压测
- ✉️ **自定义消息** 支持自定义踢出消息和日志消息
- 🔄 **热重载** 支持热重载配置文件

## 快速安装

1. 将 `forcehosts.jar` 文件放入 Velocity 服务器的 `plugins` 目录
2. 重启或启动 Velocity 服务器
3. 插件会在第一次运行时自动生成配置文件
4. 编辑生成的 `config.yml` 和 `messages.yml` 文件以满足你的需求
5. 使用 `/forcehosts reload` 命令重载配置

## 兼容性

- Velocity 3.0.0 或更高版本

## 贡献与支持

- 觉得好用可以给这个项目点个 `Star` 或者去 [`爱发电`](https://afdian.com/a/MoeLuoYu) 为我赞助。

- 有意见或者建议也欢迎提交 [`Issues`](https://github.com/MoeLuoYu/velocity-force-hosts/issues)
  和 [`Pull requests`](https://github.com/MoeLuoYu/velocity-force-hosts/pulls) 。

## 开源许可

本项目使用 [`MIT`](https://github.com/MoeLuoYu/velocity-force-hosts/blob/main/LICENSE) 作为开源许可证。
