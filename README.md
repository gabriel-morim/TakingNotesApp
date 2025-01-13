# TakingNotes App

TakingNotes é uma aplicação desenvolvida no Android Studio para facilitar a criação e a observação de notas. A ideia inicial era criar uma aplicação que mostrasse vários blocos de notas simultaneamente e que utilizasse markdown para a formatação de texto. Apesar de algumas funcionalidades planeadas não terem sido implementadas, o resultado final mantém o foco na usabilidade e segurança dos dados dos utilizadores.

## Funcionalidades Principais

- **Criação e edição de notas**: Adicione novas notas facilmente e edite-as conforme necessário.
- **Interface intuitiva**: Design limpo e fácil de usar, utilizando a biblioteca Jetpack Compose.
- **Autenticação Firebase**: Segurança garantida ao salvar notas no sistema, impedindo o acesso não autorizado.
- **Barra de navegação dinâmica**: Menu inferior que adapta a sua visibilidade de acordo com a tela atual.

## O que podia ter sido feito de forma diferente

- **Edição de texto com markdown**: A tentativa de implementar a formatação de texto em markdown não foi bem-sucedida, mas isso não prejudicou a funcionalidade principal da aplicação.
- **Exibição de múltiplos blocos de notas simultaneamente**: Após reflexão, esta funcionalidade foi considerada desnecessária para os objetivos principais do projeto.

## Tecnologias Utilizadas

- **Android Studio**: Ambiente de desenvolvimento integrado.
- **Jetpack Compose**: Biblioteca para a criação da interface de usuário.
- **Firebase**: Integração para autenticação e gestão de dados.
- **Kotlin**: Linguagem de programação principal.

## Estrutura do Projeto

- `MainActivity.kt`: Configuração principal da aplicação, inicialização do Firebase e definição da navegação entre as telas.
- **LoginToFirebase** e **RegisterToFirebase**: Para autenticação de usuários.
- **Settings**: Botão para fazer Sign Out ou Delete Account.
- **Destinos**: Gerir os destinos de cada ecrã.
- **NoteListScreen1/2**: Tela principal para visualização das notas.
- **CreateNoteScreen**: Tela dedicada para adicionar ou editar uma nota.

## Como Executar

1. Clone o repositório ou baixe os arquivos do projeto.
2. Abra o projeto no Android Studio.
4. Conecte um dispositivo Android ou utilize o simulador built-in no Android Studio.
5. Execute a aplicação através do botão **Run** no Android Studio.

## Conclusão

Embora a implementação do markdown não tenha sido concluída, a TakingNotes App cumpre seu propósito principal: fornecer um ambiente simples e seguro para a criação e o gerenciamento de notas. Com integração segura ao Firebase e uma interface responsiva, é uma ferramenta útil para estudantes e profissionais.

---

Desenvolvido por Gabriel Morim para a disciplina de Computação Móvel. 
Apresentado a 13/01/2025.
