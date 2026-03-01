import type User from '@/models/User';
import {create} from 'zustand';
import type LoginData from '@/models/LoginData';
import { loginUser, logoutUser } from '@/services/AuthService';
import type LoginResponseData from '@/models/LoginResponseData';
import {persist} from 'zustand/middleware';

const LOCAL_KEY = 'auth_state';

// type AuthStatus = "idle"| "authenticating" | "authenticated" | "error";



//global state for auth

type AuthState = {
    accessToken: string | null;
    user: User | null;
    authStatus: boolean;
    authLoading:boolean;
    login:  (loginData: LoginData) => Promise<LoginResponseData>;
    logout: (silent?:boolean) => void;
    checkLogin:() => boolean | undefined;
};



    

//main logic for global state

const useAuth = create<AuthState> () (
    persist(
        (set, get) => ({
    accessToken: null,
    user: null,
    authStatus: false,
    authLoading: false,
    login: async(loginData) =>{
        console.log("started login...")
        set({authLoading: true});
        try{
            const loginResponseData = await loginUser(loginData)
            console.log(loginResponseData);
        set({
            accessToken: loginResponseData.accessToken,
            user: loginResponseData.user,
            authStatus: true,
        });
        return loginResponseData;
        }catch(error){
            console.log("error",error)
            throw error;
        }
        finally{
            set({
                authLoading: false,
            });
        }
    },
    logout: async (silent = false) => {
        // try{
        //     if(!silent){
        //         await logoutUser();
        //     }
        // }catch(error){

        // }
        try {
            set({
                authLoading: true,
            });
            await logoutUser();
        } catch (error) {}
        finally{
            set({
                authLoading: false,
            })
        }
        // await logoutUser();
        set({
            accessToken: null,
            user: null,
            authStatus: false,
            authLoading: false,
        })
    },
    checkLogin: () => {
        if(get().accessToken && get().authStatus) return true;
        else return false;
    },
}),
{
    name: LOCAL_KEY,
}
    )
);

export default useAuth;