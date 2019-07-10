
  Pod::Spec.new do |s|
    s.name = 'OxylianCapacitorFacebookLogin'
    s.version = '1.0.0-alpha.5'
    s.summary = 'Facebook Login plugin for Capacitor'
    s.license = 'Apache-2.0'
    s.homepage = 'https://github.com/oxylian/capacitor-facebook-login'
    s.author = 'Sébastian Dejonghe'
    s.source = { :git => 'https://github.com/oxylian/capacitor-facebook-login', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
    s.dependency 'FacebookLogin'
  end
