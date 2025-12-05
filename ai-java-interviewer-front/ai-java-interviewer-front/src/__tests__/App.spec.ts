import { mount } from '@vue/test-utils';
import App from '../App.vue';

describe('App.vue', () => {
  it('renders login view by default', () => {
    const wrapper = mount(App);
    expect(wrapper.findComponent({ name: 'LoginView' }).exists()).toBe(true);
  });

  it('navigates to register view', async () => {
    const wrapper = mount(App);
    await wrapper.find('a.register-link').trigger('click');
    expect(wrapper.findComponent({ name: 'RegisterView' }).exists()).toBe(true);
  });

  it('navigates to upload resume view after login', async () => {
    const wrapper = mount(App);
    await wrapper.find('a.login-link').trigger('click'); // Simulate login
    expect(wrapper.findComponent({ name: 'UploadResumeView' }).exists()).toBe(true);
  });
});