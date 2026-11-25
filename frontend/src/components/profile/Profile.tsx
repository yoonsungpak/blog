import Image from "next/image";
import styles from "./Profile.module.css";

const profile: { name: string; bio: string; photo: string | null } = {
    name: "샘플 프로필",
    bio: "웹 개발과 일상에서 배운 것을 기록하는 예시 블로그입니다.",
    // Set a path relative to public/, for example "/profile.jpg".
    photo: null,
};

export default function Profile() {
    return (
        <section className={`${styles.profile} d-flex align-items-center gap-3`} aria-label="프로필">
            <div className={styles.avatar} aria-hidden="true">
                {profile.photo ? <Image src={profile.photo} alt="" width={64} height={64} /> : profile.name[0]}
            </div>
            <div>
                <p className={styles.profileName}>{profile.name}</p>
                <p className={styles.profileBio}>{profile.bio}</p>
            </div>
        </section>
    );
}
