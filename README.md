# 🚀 hAmongUS - Plugin Among Us para Minecraft 1.8.9

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.9-green.svg)](https://www.minecraft.net/)
[![Spigot](https://img.shields.io/badge/Spigot-1.8.9-yellow.svg)](https://www.spigotmc.org/)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://www.java.com/)

> **⚠️ ATENÇÃO:** Este é um projeto em desenvolvimento baseado na source do kMurder. Contém alguns bugs conhecidos e está em versão beta.

## 📋 Sobre o Projeto

O **hAmongUS** é um plugin completo de Among Us para Minecraft 1.8.9, desenvolvido a partir da base do plugin kMurder. O projeto transforma completamente a mecânica do Murder Mystery em um minigame fiel ao Among Us original.

### 🎯 Características Principais

- **🎭 Sistema de Papéis:** Tripulantes e Impostores com atribuição automática
- **📋 Sistema de Tarefas:** 8+ tarefas únicas adaptadas ao Minecraft
- **🔧 Sistema de Sabotagem:** Luzes, oxigênio e portas
- **🗣️ Reuniões e Votação:** Chat de discussão e sistema de votação
- **⚔️ Sistema de Kill:** Cooldown configurável para impostores
- **🗺️ Múltiplos Mapas:** Suporte a configurações de mapa personalizadas
- **📊 Interface HUD:** BossBar de progresso e scoreboard informativo
- **⚙️ Configuração Completa:** Tudo configurável via config.yml

## 🛠️ Tecnologias Utilizadas

- **Java 8**
- **Spigot 1.8.9 API**
- **Bukkit API**
- **Maven** (Gerenciamento de dependências)
- **YAML** (Configurações)

## 📦 Instalação

### Pré-requisitos
- Java 8 ou superior
- Spigot 1.8.9
- Maven (para compilação)

### Compilação
```bash
# Clone o repositório
git clone https://github.com/jvwp7/hAmongUS.git

# Entre no diretório
cd hAmongUS

# Compile o projeto
mvn clean package

# O arquivo .jar será gerado em target/hAmongUS-1.0.jar
```

### Instalação no Servidor
1. Copie o arquivo `hAmongUS-1.0.jar` para a pasta `plugins/`
2. Reinicie o servidor
3. Configure o plugin através dos arquivos gerados em `plugins/hAmongUS/`

## 🎮 Como Jogar

### Comandos Principais
- `/am criar [modo] [nome]` - Criar um novo mapa
- `/am sala [nome]` - Adicionar salas ao mapa
- `/am tarefa` - Abrir menu de seleção de tarefas
- `/am vent [adicionar/conectar]` - Gerenciar ventilações
- `/am emergencia` - Adicionar botão de emergência
- `/am sabotagem [tipo]` - Adicionar sabotagens

### Modos de Jogo
- **Impostor 1:** 1 Impostor, 7 Tripulantes
- **Impostor 2:** 2 Impostores, 10 Tripulantes  
- **Impostor 3:** 3 Impostores, 13 Tripulantes

## ⚠️ Bugs Conhecidos

Este projeto está em desenvolvimento e contém alguns bugs conhecidos:

- [ ] Alguns comandos podem não funcionar corretamente
- [ ] Interface pode apresentar problemas visuais
- [ ] Sistema de tarefas pode ter falhas
- [ ] Carregamento de mapas pode gerar erros
- [ ] Sistema de votação pode não funcionar perfeitamente

## 🔧 Configuração

### Arquivos de Configuração
- `config.yml` - Configurações principais
- `messages.yml` - Mensagens personalizáveis
- `arenas/` - Configurações de mapas
- `mundos/` - Backups dos mundos

### Exemplo de Configuração
```yaml
# config.yml
spawn: 'world,0,64,0,0,0'
kill_cooldown: 30
meeting_time: 15
voting_time: 120
task_time: 5
player_speed: 1.0
kill_distance: 1.5
```

## 🤝 Contribuição

Este projeto está em desenvolvimento ativo. Se você encontrar bugs ou quiser contribuir:

1. Abra uma **Issue** descrevendo o problema
2. Faça um **Fork** do projeto
3. Crie uma **Branch** para sua feature
4. Faça **Commit** das suas mudanças
5. Abra um **Pull Request**

## 📞 Suporte e Versão Final

### 🎯 Versão Final Completa
Para adquirir a **versão final completa** sem bugs e com todas as funcionalidades implementadas:

**📱 Entre em contato via Discord:**
- **Discord:** `jvwp7`
- **Mensagem:** "Olá! Gostaria de adquirir a versão final do hAmongUS"

### 💰 Informações sobre a Versão Final
- ✅ **100% funcional** sem bugs
- ✅ **Todas as mecânicas** do Among Us implementadas
- ✅ **Sistema de tarefas** completo
- ✅ **Interface otimizada**
- ✅ **Suporte técnico** incluído
- ✅ **Atualizações** gratuitas

## 🙏 Agradecimentos

- **kMurder** - Base original do projeto
- **SpigotMC** - API do Minecraft
- **Comunidade Minecraft** - Testes e feedback

## 📊 Status do Projeto

![Progresso](https://img.shields.io/badge/Progresso-75%25-yellow.svg)
![Versão](https://img.shields.io/badge/Versão-Beta-orange.svg)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-blue.svg)

---

**⭐ Se este projeto te ajudou, deixe uma estrela no repositório!**

**🔗 Links Úteis:**
- [Issues](https://github.com/jvwp7/hAmongUS/issues)
- [Discord](https://discord.com/users/632982949842649098)
- [Wiki](https://github.com/jvwp7/hAmongUS/wiki)

