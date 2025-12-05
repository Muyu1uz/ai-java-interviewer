import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
    test('Login and Upload Resume', async ({ page }) => {
        // Navigate to the login page
        await page.goto('http://localhost:3000/login');

        // Fill in the login form
        await page.fill('input[name="username"]', 'testuser');
        await page.fill('input[name="password"]', 'password123');
        await page.click('button[type="submit"]');

        // Expect to be redirected to the upload resume page
        await expect(page).toHaveURL('http://localhost:3000/upload');

        // Upload a resume
        const filePath = 'path/to/resume.pdf'; // Update with the actual path to the resume file
        await page.setInputFiles('input[type="file"]', filePath);
        await page.click('button[type="submit"]');

        // Expect a success message or confirmation
        await expect(page.locator('.success-message')).toBeVisible();
    });

    test('Register and Login', async ({ page }) => {
        // Navigate to the register page
        await page.goto('http://localhost:3000/register');

        // Fill in the registration form
        await page.fill('input[name="username"]', 'newuser');
        await page.fill('input[name="password"]', 'newpassword123');
        await page.click('button[type="submit"]');

        // Expect to be redirected back to the login page
        await expect(page).toHaveURL('http://localhost:3000/login');

        // Fill in the login form with new user credentials
        await page.fill('input[name="username"]', 'newuser');
        await page.fill('input[name="password"]', 'newpassword123');
        await page.click('button[type="submit"]');

        // Expect to be redirected to the upload resume page
        await expect(page).toHaveURL('http://localhost:3000/upload');
    });
});