# Imagen para compilar la app sin instalar JDK ni Android SDK en el equipo.
#   docker build -t cineillojuan-build -f tools/android-build.Dockerfile tools
#   docker run --rm -v "$PWD:/project" -v cineillojuan-gradle:/home/gradle/.gradle -v cineillojuan-m2:/root/.m2 -w /project cineillojuan-build ./gradlew assembleDebug
FROM gradle:8.11.1-jdk17

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools"

RUN apt-get update && apt-get install -y --no-install-recommends unzip wget && rm -rf /var/lib/apt/lists/* \
 && mkdir -p $ANDROID_HOME/cmdline-tools \
 && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/tools.zip \
 && unzip -q /tmp/tools.zip -d $ANDROID_HOME/cmdline-tools \
 && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest \
 && rm /tmp/tools.zip \
 && yes | sdkmanager --licenses > /dev/null \
 && sdkmanager "platforms;android-35" "build-tools;35.0.0" "build-tools;34.0.0" "platform-tools" > /dev/null \
 && chmod -R a+rwX $ANDROID_HOME
