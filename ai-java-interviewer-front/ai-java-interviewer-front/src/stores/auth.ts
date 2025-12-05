import { reactive } from 'vue';

interface AuthState {
  isAuthenticated: boolean;
  user: null | { username: string; email: string };
}

const state = reactive<AuthState>({
  isAuthenticated: false,
  user: null,
});

const login = (username: string, password: string) => {
  // Implement login logic here
  // On successful login, update state
  state.isAuthenticated = true;
  state.user = { username, email: 'user@example.com' }; // Replace with actual user data
};

const logout = () => {
  // Implement logout logic here
  state.isAuthenticated = false;
  state.user = null;
};

const register = (username: string, email: string, password: string) => {
  // Implement registration logic here
  // On successful registration, you might want to log the user in
  login(username, password);
};

export default {
  state,
  login,
  logout,
  register,
};