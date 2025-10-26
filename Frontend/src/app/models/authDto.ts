export interface LoginRequestDTO {
  email: string;
  password: string;
}

export interface RegisterRequestDTO {
  name: string;
  email: string;
  password: string;
  phone?: string;
  bio?: string;
}

export interface AuthResponseDTO {
  token: string;
}
