import "server-only";
import { getSessionBySid } from "@/lib/auth/getCurrentSession";
import UserRepository from "../repository/UserRepository";

export default class UserService {
    private readonly userRepository = new UserRepository();

    public async isAdmin(sessionId: string): Promise<boolean> {
        const session = await getSessionBySid(sessionId);
        if (!session?.username) {
            return false;
        }

        const user = await this.userRepository.findByUsername(session.username);
        return user?.role === "ADMIN";
    }
}
