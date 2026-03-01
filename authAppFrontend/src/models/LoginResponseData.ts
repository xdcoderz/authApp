import type User from '@/models/User';

export default interface LoginResponseData {
    accessToken: string;
    user: User;
    refreshToken: string;
    expiresIn: number;
}
