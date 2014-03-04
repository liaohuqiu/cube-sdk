module Jekyll

  class Site
    alias :process_org :process
    def process

      self.exclude << 'lang'
      self.config['exclude'] << 'lang'

      baseurl = self.config['baseurl']
      if !baseurl || baseurl == '/'
        baseurl = ""
      end
      self.config['baseurl'] = baseurl
      self.baseurl = baseurl

      #Origin variables
      baseurl_org = baseurl
      dest_org = self.dest
      languages = self.config['languages']

      puts
      puts 'Begin build for multiple languages'
      #Default site
      self.config['lang'] = languages.first
      inner_process

      # Other languages
      languages.drop(1).each do |lang|

        # Build site for language
        self.dest = File.join(self.dest, lang)
        self.config['baseurl'] = File.join(self.config['baseurl'], lang)
        self.config['lang'] = lang

        inner_process
      end

      # Reset variables
      self.dest = dest_org
      self.config['baseurl'] = baseurl_org
      puts 'Build complete'
    end

    def inner_process()
      puts "Try to building site for language: \"#{self.config['lang']}\" to: " + self.dest
      process_org
      puts
    end

    def read
      self.read_layouts

      lang_path = "/lang/" + self.config['lang']
      if File.exists?(File.join(self.source, lang_path))
        self.read_directories(lang_path)
      else
        puts "Language directory is not exist: " + lang_path
      end
      self.read_directories
      self.read_data(config['data_source'])
    end
  end

  class Post
    alias :destination_org :destination
    def destination(dest)
      path = destination_org(dest)
      if path && path != ''
        path.slice! '/lang/' + self.site.config['lang']
      end
      path
    end
  end

  class Page
    alias :destination_org :destination
    def destination(dest)
      path = destination_org(dest)
      if path && path != ''
        path.slice! '/lang/' + self.site.config['lang']
      end
      path
    end
  end

  class LocalizeTag < Liquid::Tag

    def initialize(tag_name, key, tokens)
      super
      @key = key.strip
    end

    def render(context)
      if "#{context[@key]}" != "" #Check for page variable
        key = "#{context[@key]}"
      else
        key = @key
      end
      lang = context.registers[:site].config['lang']
      candidate = YAML.load_file(context.registers[:site].source + "/lang/#{lang}.yml")
      path = key.split(/\./) if key.is_a?(String)
      while !path.empty?
        key = path.shift
        if candidate[key]
          candidate = candidate[key]
        else
          candidate = ""
        end
      end
      if candidate == ""
        puts "Missing i18n key: " + lang + ":" + key
        "*" + lang + ":" + key + "*"
      else
        candidate
      end
    end
  end

  module Tags
    class LocalizeInclude < IncludeTag
      def render(context)
        if "#{context[@file]}" != "" #Check for page variable
          file = "#{context[@file]}"
        else
          file = @file
        end

        includes_dir = File.join(context.registers[:site].source, 'lang/' + context.registers[:site].config['lang'])

        if File.symlink?(includes_dir)
          return "Includes directory '#{includes_dir}' cannot be a symlink"
        end
        if file !~ /^[a-zA-Z0-9_\/\.-]+$/ || file =~ /\.\// || file =~ /\/\./
          return "Include file '#{file}' contains invalid characters or sequences"
        end

        Dir.chdir(includes_dir) do
          choices = Dir['**/*'].reject { |x| File.symlink?(x) }
          if choices.include?(file)
            source = File.read(file)
            partial = Liquid::Template.parse(source)

            context.stack do
              context['include'] = parse_params(context) if @params
              contents = partial.render(context)
              site = context.registers[:site]
              ext = File.extname(file)

              converter = site.converters.find { |c| c.matches(ext) }
              contents = converter.convert(contents) unless converter.nil?

              contents
            end
          else
            "Included file '#{file}' not found in #{includes_dir} directory"
          end
        end
      end
    end
  end
end
