import os
import uuid
import threading
from locust import HttpUser, task, between

# Controle global thread-safe para realizar o cadastro apenas uma vez
_register_lock = threading.Lock()
_user_registered = False

# Geramos credenciais únicas para cada execução de teste se não forem especificadas nas env vars.
# Isso evita conflitos de chaves duplicadas e senhas antigas persistidas no banco de dados.
_env_email = os.getenv("AUTH_EMAIL")
if _env_email:
    _unique_email = _env_email
    _is_custom = True
else:
    _unique_email = f"locust_{uuid.uuid4().hex[:8]}@shop.com"
    _is_custom = False

_unique_password = os.getenv("AUTH_PASSWORD", "SecurePass123!")

class APIUser(HttpUser):
    # Tempo de espera entre as tarefas de cada usuário simulado (em segundos)
    wait_time = between(1, 5)
    
    # Configurações de autenticação com base na API Java do Repair Shop
    auth_url = "/auth/login"
    email = _unique_email
    password = _unique_password
    
    token = None

    def on_start(self):
        """
        Executado quando cada usuário simulado inicia.
        Garante o cadastro único global do usuário e depois realiza o login.
        Se a autenticação falhar (por exemplo, se o banco foi reiniciado/limpo
        ou se o cadastro inicial falhou por lentidão no startup), tenta registrar novamente.
        """
        global _user_registered
        
        # Garante que apenas o primeiro usuário tente cadastrar inicialmente
        with _register_lock:
            if not _user_registered:
                self.register_user()
                _user_registered = True
                
        # Todos os usuários fazem login para obter seus próprios tokens
        self.authenticate()

        # Mecanismo de auto-correção: se o token não foi obtido (falha no login),
        # tenta realizar o cadastro novamente e re-autenticar
        if not self.token:
            print(f"[Autenticação] Token não obtido para {self.email}. Tentando registrar novamente para auto-correção...")
            self.register_user()
            self.authenticate()

    def register_user(self):
        """
        Realiza o cadastro único do usuário administrador.
        """
        register_url = "/auth/register"
        register_payload = {
            "name": "Locust Admin User",
            "function": "ATTENDANT",
            "email": self.email,
            "phone": "+55 11 99999-9999",
            "password": self.password
        }
        
        headers = {
            "Content-Type": "application/json"
        }

        print(f"[Cadastro] Tentando realizar o cadastro único de {self.email}...")
        with self.client.post(register_url, json=register_payload, headers=headers, catch_response=True) as response:
            if response.status_code == 201:
                response.success()
                print(f"[Cadastro] Usuário {self.email} cadastrado com sucesso!")
            elif response.status_code in [400, 409]:
                # Conflito ou já cadastrado
                response.success()
                print(f"[Cadastro] Usuário {self.email} já estava cadastrado ou conflito retornado (ignorado).")
            else:
                response.failure(f"[Cadastro] Erro ao cadastrar {self.email}. Status: {response.status_code}, Resposta: {response.text}")

    def authenticate(self):
        """
        Realiza o login para obter o Bearer Token.
        """
        payload = {
            "email": self.email,
            "password": self.password
        }
        
        headers = {
            "Content-Type": "application/json"
        }

        with self.client.post(self.auth_url, json=payload, headers=headers, catch_response=True) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.token = data.get("token")
                    response.success()
                    print(f"[Login] Autenticação de {self.email} realizada com sucesso!")
                except ValueError:
                    response.failure("[Login] Resposta de autenticação não é um JSON válido")
            else:
                response.failure(f"[Login] Falha na autenticação de {self.email}. Status: {response.status_code}, Resposta: {response.text}")

    @task(1)
    def list_service_orders(self):
        """
        Tarefa que realiza requisições GET para listar as ordens de serviço.
        """
        if not self.token:
            self.authenticate()
            if not self.token:
                print("Ignorando chamada pois não há token de autenticação.")
                return

        headers = {
            "Authorization": f"Bearer {self.token}",
            "Content-Type": "application/json"
        }

        # O endpoint aceita paginação através dos query params (ex: page, size, sort)
        target_endpoint = "/service-orders?page=0&size=10"
        
        with self.client.get(target_endpoint, headers=headers, catch_response=True) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"Erro ao listar ordens de serviço. Status: {response.status_code}")
