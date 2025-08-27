# sh build-backend.sh admin 250826

echo "도메인: $1"
echo "도메인버전: $2"

cd $1
# 자바 패키지 생성
mvn package -B -DskipTests
echo "자바 패키지 생성 완료"

# 이미지 생성
docker build -t leeyumin/$1:$2 .
echo "이미지 생성 완료"
docker push leeyumin/$1:$2
echo "이미지 push 완료"


